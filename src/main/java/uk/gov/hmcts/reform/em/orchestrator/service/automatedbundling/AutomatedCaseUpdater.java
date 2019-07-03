package uk.gov.hmcts.reform.em.orchestrator.service.automatedbundling;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleFolderDTO;

/**
 * This class will update add a new bundle to case based on some predefined configuration
 */
public class AutomatedCaseUpdater implements CcdCaseUpdater {
    private static final String CONFIG_FIELD = "bundleConfiguration";
    private final ConfigurationLoader configurationLoader;

    public AutomatedCaseUpdater(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
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
        String filename = ccdCallbackDto.getCaseData().get(CONFIG_FIELD).asText();

        // todo add errors (config not found or invalid, could not serialize
        return configurationLoader
            .load(filename)
            .map(c -> createBundle(c, ccdCallbackDto.getCaseData()))
            .orElse(ccdCallbackDto.getCaseData());
    }

    private JsonNode createBundle(BundleConfiguration configuration, JsonNode caseData) {
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setTitle(configuration.title);
        int sortIndex = 0;

        for (BundleConfigurationFolder folder : configuration.folders) {
            CcdBundleFolderDTO ccdFolder = new CcdBundleFolderDTO();
            ccdFolder.setName(folder.name);
            ccdFolder.setSortIndex(sortIndex++);
        }

        // todo actually update the case data
        // caseData.documentBundles.add(newBundle)

        return caseData;
    }
}
