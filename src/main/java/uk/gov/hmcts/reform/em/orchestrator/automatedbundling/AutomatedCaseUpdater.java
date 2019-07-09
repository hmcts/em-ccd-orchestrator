package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfiguration;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfigurationFolder;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.ConfigurationLoader;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBoolean;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleFolderDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.util.List;

/**
 * This class will update add a new bundle to case based on some predefined configuration
 */
public class AutomatedCaseUpdater implements CcdCaseUpdater {
    private static final String CONFIG_FIELD = "bundleConfiguration";
    private final ConfigurationLoader configurationLoader;
    private final ObjectMapper jsonMapper;

    public AutomatedCaseUpdater(ConfigurationLoader configurationLoader, ObjectMapper jsonMapper) {
        this.configurationLoader = configurationLoader;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public boolean handles(CcdCallbackDto ccdCallbackDto) {
        return ccdCallbackDto.getCaseData().has(CONFIG_FIELD);
    }

    /**
     * Load the configuration file then add a new bundle to the case data based on that configuration. If an error occurs
     * during loading or processing the original case data will be returned with errors
     */
    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {
        String configurationName = ccdCallbackDto.getCaseData().get(CONFIG_FIELD).asText();
        BundleConfiguration configuration = configurationLoader.load(configurationName);
        ArrayNode bundles = ccdCallbackDto
            .findCaseProperty(ArrayNode.class)
            .orElseGet(() -> {
                ArrayNode arrayNode = jsonMapper.createArrayNode();
                ((ObjectNode)ccdCallbackDto.getCaseData()).set(ccdCallbackDto.getPropertyName().get(), arrayNode);
                return arrayNode;
            });

        addNewBundle(configuration, bundles);

        return ccdCallbackDto.getCaseData();
    }

    private void addNewBundle(BundleConfiguration configuration, ArrayNode bundles) {
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setTitle(configuration.title);
        bundle.setHasCoversheets(configuration.hasCoversheets);
        bundle.setHasTableOfContents(configuration.hasTableOfContents);
        bundle.setHasFolderCoversheets(configuration.hasFolderCoversheets);
        bundle.setFileName(configuration.filename);
        bundle.setEligibleForCloningAsBoolean(false);
        bundle.setEligibleForStitchingAsBoolean(false);

        addFolders(configuration.folders, bundle.getFolders(), 0);

        bundles.add(bundleDtoToBundleJson(bundle));
    }

    private int addFolders(List<BundleConfigurationFolder> sourceFolders,
                           List<CcdValue<CcdBundleFolderDTO>> destinationFolders,
                           int sortIndex) {

        for (BundleConfigurationFolder folder : sourceFolders) {
            CcdBundleFolderDTO ccdFolder = new CcdBundleFolderDTO();
            ccdFolder.setName(folder.name);
            ccdFolder.setSortIndex(sortIndex++);

            destinationFolders.add(new CcdValue<>(ccdFolder));

            if (folder.folders != null && !folder.folders.isEmpty()) {
                sortIndex = addFolders(folder.folders, ccdFolder.getFolders(), sortIndex);
            }
        }

        return sortIndex;
    }

    private JsonNode bundleDtoToBundleJson(CcdBundleDTO ccdBundle) {
        CcdValue<CcdBundleDTO> ccdValue = new CcdValue<>();
        ccdValue.setValue(ccdBundle);
        return jsonMapper.convertValue(ccdValue, JsonNode.class);
    }
}
