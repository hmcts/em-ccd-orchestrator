package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.BundleConfiguration;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.ConfigurationLoader;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.InputValidationException;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class will update add a new bundle to case based on some predefined configuration.
 */
public class AutomatedCaseUpdater implements CcdCaseUpdater {

    private final Logger log = LoggerFactory.getLogger(AutomatedCaseUpdater.class);

    private static final String CONFIG_FIELD = "bundleConfiguration";
    private static final String CASE_ID = "id";
    private static final Map<String, String> CONFIG_MAP = ImmutableMap.of("SSCS", "sscs-bundle-config.yaml");
    private static final String DEFAULT_CONFIG = "default-config.yaml";

    private final ConfigurationLoader configurationLoader;
    private final ObjectMapper jsonMapper;
    private final BundleFactory bundleFactory;
    private final AutomatedStitchingExecutor automatedStitchingExecutor;
    private final Validator validator;

    public AutomatedCaseUpdater(ConfigurationLoader configurationLoader,
                                ObjectMapper jsonMapper,
                                BundleFactory bundleFactory,
                                AutomatedStitchingExecutor automatedStitchingExecutor,
                                Validator validator) {
        this.configurationLoader = configurationLoader;
        this.jsonMapper = jsonMapper;
        this.bundleFactory = bundleFactory;
        this.automatedStitchingExecutor = automatedStitchingExecutor;
        this.validator = validator;
    }

    /**
     * Load the configuration file then add a new bundle to the case data based on that configuration. If an error occurs
     * during loading or processing the original case data will be returned with errors
     */
    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {

        List<String> bundleConfigurations = new ArrayList<String>();

        populateBundleConfigs(ccdCallbackDto, bundleConfigurations);

        // Need to validate all BundleConfigs before invoking the stitching call.
        for (String bundleConfig : bundleConfigurations) {

            BundleConfiguration configuration = configurationLoader.load(bundleConfig);
            final ArrayNode bundles = ccdCallbackDto
                .findCaseProperty(ArrayNode.class)
                .orElseGet(() -> {
                    ArrayNode arrayNode = jsonMapper.createArrayNode();
                    ((ObjectNode)ccdCallbackDto.getCaseData()).set(ccdCallbackDto.getPropertyName().get(), arrayNode);
                    return arrayNode;
                });

            CcdBundleDTO bundle = bundleFactory.create(configuration, ccdCallbackDto.getCaseData());
            ccdCallbackDto.setEnableEmailNotification(bundle.getEnableEmailNotificationAsBoolean());
            if (bundle.getFileNameIdentifier() != null && !bundle.getFileNameIdentifier().isEmpty()) {
                bundle.setFileName(ccdCallbackDto.getIdentifierFromCcdPayload(bundle.getFileNameIdentifier()) + "-" + bundle.getFileName());
            }
            bundle.setCoverpageTemplateData(ccdCallbackDto.getCaseDetails());

            Set<ConstraintViolation<CcdBundleDTO>> violations = validator.validate(bundle);

            if (!violations.isEmpty()) {
                throw new InputValidationException(violations);
            }

            automatedStitchingExecutor.startStitching(
                ccdCallbackDto.getCaseId(),
                ccdCallbackDto.getJwt(),
                bundle);

            bundles.insert(0, bundleDtoToBundleJson(bundle));
        }

        return ccdCallbackDto.getCaseData();
    }

    private void populateBundleConfigs(CcdCallbackDto ccdCallbackDto, List<String> bundleConfigurations) {

        try {
            if (ccdCallbackDto.getCaseData().has(CONFIG_FIELD)) {
                if (ccdCallbackDto.getCaseData().get(CONFIG_FIELD).isArray()) {
                    if (!ccdCallbackDto.getCaseData().get(CONFIG_FIELD).isEmpty()) {
                        bundleConfigurations.addAll(jsonMapper.readValue(ccdCallbackDto.getCaseData().get(CONFIG_FIELD).toString(),
                                List.class));
                    }
                } else {
                    bundleConfigurations.add(ccdCallbackDto.getCaseData().get(CONFIG_FIELD).asText());
                }
            }
        } catch (JsonProcessingException jexp) {
            log.error(String.format("Error parsing request for Case-Id  : %s",ccdCallbackDto.getCaseDetails().get(CASE_ID)));
        }
        if (CollectionUtils.isEmpty(bundleConfigurations)) {
            bundleConfigurations.add(CONFIG_MAP.getOrDefault(ccdCallbackDto.getJurisdiction(), DEFAULT_CONFIG));
        }
    }

    private JsonNode bundleDtoToBundleJson(CcdBundleDTO ccdBundle) {
        CcdValue<CcdBundleDTO> ccdValue = new CcdValue<>();
        ccdValue.setValue(ccdBundle);
        return jsonMapper.convertValue(ccdValue, JsonNode.class);
    }
}
