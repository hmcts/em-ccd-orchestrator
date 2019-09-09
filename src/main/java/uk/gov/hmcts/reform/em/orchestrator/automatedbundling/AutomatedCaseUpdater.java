package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfiguration;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.ConfigurationLoader;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

/**
 * This class will update add a new bundle to case based on some predefined configuration.
 */
public class AutomatedCaseUpdater implements CcdCaseUpdater {
    private static final String CONFIG_FIELD = "bundleConfiguration";
    private final ConfigurationLoader configurationLoader;
    private final ObjectMapper jsonMapper;
    private final BundleFactory bundleFactory;
    private final AutomatedStitchingExecutor automatedStitchingExecutor;

    public AutomatedCaseUpdater(ConfigurationLoader configurationLoader,
                                ObjectMapper jsonMapper,
                                BundleFactory bundleFactory,
                                AutomatedStitchingExecutor automatedStitchingExecutor) {
        this.configurationLoader = configurationLoader;
        this.jsonMapper = jsonMapper;
        this.bundleFactory = bundleFactory;
        this.automatedStitchingExecutor = automatedStitchingExecutor;
    }

    @Override
    public boolean handles(CcdCallbackDto ccdCallbackDto) {
        JsonNode caseData =  ccdCallbackDto.getCaseData();
        return (caseData.has(CONFIG_FIELD)
                && caseData.get(CONFIG_FIELD) != null
                && !caseData.get(CONFIG_FIELD).asText().isEmpty());
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

        CcdBundleDTO bundle = bundleFactory.create(configuration, ccdCallbackDto.getCaseData());
        bundles.add(bundleDtoToBundleJson(bundle));

        automatedStitchingExecutor.startStitching(
                ccdCallbackDto.getCaseId(),
                "ASYNC_STITCHING_COMPLETE",
                ccdCallbackDto.getJwt(),
                bundle);

        return ccdCallbackDto.getCaseData();
    }

    private JsonNode bundleDtoToBundleJson(CcdBundleDTO ccdBundle) {
        CcdValue<CcdBundleDTO> ccdValue = new CcdValue<>();
        ccdValue.setValue(ccdBundle);
        return jsonMapper.convertValue(ccdValue, JsonNode.class);
    }
}
