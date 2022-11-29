package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfiguration;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.ConfigurationLoader;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CdamDto;
import uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class will update add a new bundle to case based on some predefined configuration.
 */
@SuppressWarnings("squid:S4738")
public class AutomatedCaseUpdater implements CcdCaseUpdater {
    private final Logger logger = LoggerFactory.getLogger(AutomatedCaseUpdater.class);

    private static final String CONFIG_FIELD = "bundleConfiguration";
    private static final String MULTI_BUNDLE_CONFIG_FIELD = "multiBundleConfiguration";
    private static final String VALUE = "value";
    private static final Map<String, String> CONFIG_MAP = ImmutableMap.of("SSCS", "sscs-bundle-config.yaml");
    private static final String DEFAULT_CONFIG = "default-config.yaml";

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

    /**
     * Load the configuration file then add a new bundle to the case data based on that configuration. If an error occurs
     * during loading or processing the original case data will be returned with errors
     */
    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {

        List<String> bundleConfigurations = prepareBundleConfigs(ccdCallbackDto);

        List<CcdBundleDTO>  ccdBundleDtos = populateBundleConfigs(ccdCallbackDto, bundleConfigurations);

        //Make call for Stitching only after the validation is completed for the Bundles and have no validation error.
        for (CcdBundleDTO bundle : ccdBundleDtos) {
            final ArrayNode bundles = ccdCallbackDto.findCaseProperty(ArrayNode.class).orElseGet(() -> {
                ArrayNode arrayNode = jsonMapper.createArrayNode();
                ((ObjectNode) ccdCallbackDto.getCaseData()).set(ccdCallbackDto.getPropertyName().get(), arrayNode);
                return arrayNode;
            });

            CdamDto cdamDto = StringUtilities.populateCdamDetails(ccdCallbackDto);

            long documentTaskId = automatedStitchingExecutor.startStitching(cdamDto, bundle);
            logger.info("startStitching documentTaskId:{}", documentTaskId);
            ccdCallbackDto.setDocumentTaskId(documentTaskId);

            bundles.insert(0, bundleDtoToBundleJson(bundle));
        }

        try {
            logger.info("AutomatedCaseUpdater response ccdCallbackDto.getCaseData {}",
                    jsonMapper.writeValueAsString(ccdCallbackDto.getCaseData()));
        } catch (JsonProcessingException e) {
            logger.error("AutomatedCaseUpdater response ccdCallbackDto JsonProcessingException {}", e);
        }

        return ccdCallbackDto.getCaseData();
    }

    private List<CcdBundleDTO> populateBundleConfigs(CcdCallbackDto ccdCallbackDto, List<String> bundleConfigurations) {
        List<CcdBundleDTO>  ccdBundleDtos = new ArrayList<>();
        for (String bundleConfig : bundleConfigurations) {

            BundleConfiguration configuration = configurationLoader.load(bundleConfig);

            CcdBundleDTO bundle = bundleFactory.create(configuration, ccdCallbackDto.getCaseData());
            ccdCallbackDto.setEnableEmailNotification(bundle.getEnableEmailNotificationAsBoolean());
            if (StringUtils.isNotBlank(bundle.getFileNameIdentifier())) {
                bundle.setFileName(ccdCallbackDto.getIdentifierFromCcdPayload(bundle.getFileNameIdentifier()) + "-" + bundle.getFileName());
            }
            bundle.setCoverpageTemplateData(ccdCallbackDto.getCaseDetails());

            logger.info("bundles.... {} ", bundle);
            ccdBundleDtos.add(bundle);
        }
        return ccdBundleDtos;
    }

    private List<String> prepareBundleConfigs(CcdCallbackDto ccdCallbackDto) {

        logger.info("ccdCallbackDto {}", ccdCallbackDto);
        List<String> bundleConfigurations = new ArrayList<>();

        if (ccdCallbackDto.getCaseData().has(MULTI_BUNDLE_CONFIG_FIELD)
            && !ccdCallbackDto.getCaseData().get(MULTI_BUNDLE_CONFIG_FIELD).isEmpty()) {


            ccdCallbackDto.getCaseData().get(MULTI_BUNDLE_CONFIG_FIELD)
                .forEach(bundleConfig -> bundleConfigurations.add(bundleConfig.get(VALUE).textValue()));


        } else if (ccdCallbackDto.getCaseData().has(CONFIG_FIELD)
            && !ccdCallbackDto.getCaseData().get(CONFIG_FIELD).asText().equals("null")) {

            bundleConfigurations.add(ccdCallbackDto.getCaseData().get(CONFIG_FIELD).asText());
        }

        logger.info("bundleConfigurations {}", bundleConfigurations);
        if (CollectionUtils.isEmpty(bundleConfigurations)) {
            bundleConfigurations.add(CONFIG_MAP.getOrDefault(ccdCallbackDto.getJurisdiction(), DEFAULT_CONFIG));
            logger.info("was empty bundleConfigurations {}", bundleConfigurations);
        }
        return bundleConfigurations;
    }

    private JsonNode bundleDtoToBundleJson(CcdBundleDTO ccdBundle) {
        CcdValue<CcdBundleDTO> ccdValue = new CcdValue<>();
        ccdValue.setValue(ccdBundle);
        return jsonMapper.convertValue(ccdValue, JsonNode.class);
    }
}
