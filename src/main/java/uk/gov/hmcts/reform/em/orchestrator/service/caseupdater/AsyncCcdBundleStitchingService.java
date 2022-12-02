package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedStitchingExecutor;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CdamDto;
import uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static pl.touk.throwing.ThrowingFunction.unchecked;

@Service
@Transactional
public class AsyncCcdBundleStitchingService implements CcdCaseUpdater {
    private static Logger log = LoggerFactory.getLogger(AsyncCcdBundleStitchingService.class);

    private final ObjectMapper objectMapper;
    private final JavaType type;
    private final Validator validator;
    private final AutomatedStitchingExecutor automatedStitchingExecutor;

    public AsyncCcdBundleStitchingService(ObjectMapper objectMapper,
                                          AutomatedStitchingExecutor automatedStitchingExecutor,
                                          Validator validator) {
        this.objectMapper = objectMapper;
        this.automatedStitchingExecutor = automatedStitchingExecutor;
        type = objectMapper.getTypeFactory().constructParametricType(CcdValue.class, CcdBundleDTO.class);
        this.validator = validator;
    }

    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {
        Optional<ArrayNode> maybeBundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);

        if (maybeBundles.isPresent()) {
            List<JsonNode> newBundles = StreamSupport
                    .stream(Spliterators.spliteratorUnknownSize(maybeBundles.get().iterator(), Spliterator.ORDERED), false)
                    .parallel()
                    .map(unchecked(this::bundleJsonToBundleValue))
                    .map(bundle -> bundle.getValue().getEligibleForStitchingAsBoolean()
                            ? this.stitchBundle(ccdCallbackDto.getCaseId(), bundle, ccdCallbackDto) : bundle)
                    .map(bundleDto -> objectMapper.convertValue(bundleDto, JsonNode.class))
                    .collect(Collectors.toList());

            maybeBundles.get().removeAll();
            maybeBundles.get().addAll(CcdCaseUpdater.reorderBundles(newBundles, objectMapper, type));
            try {
                log.info("updateCase maybeBundles {}", objectMapper.writeValueAsString(maybeBundles));
            } catch (JsonProcessingException e) {
                log.error("updateCase JsonProcessingException ", e);
            }
        }

        try {
            log.info("response ccdCallbackDto.getCaseData {}", objectMapper.writeValueAsString(ccdCallbackDto.getCaseData()));
        } catch (JsonProcessingException e) {
            log.error("response error ccdCallbackDto.getCaseData ", e);
        }
        return ccdCallbackDto.getCaseData();
    }

    private CcdValue<CcdBundleDTO> stitchBundle(String caseId, CcdValue<CcdBundleDTO> bundle, CcdCallbackDto ccdCallbackDto) {
        bundle.getValue().setCoverpageTemplateData(ccdCallbackDto.getCaseDetails());
        ccdCallbackDto.setEnableEmailNotification(bundle.getValue().getEnableEmailNotificationAsBoolean());
        Set<ConstraintViolation<CcdBundleDTO>> violations = validator.validate(bundle.getValue());

        if (!violations.isEmpty()) {
            throw new InputValidationException(violations);
        }

        CdamDto cdamDto = StringUtilities.populateCdamDetails(ccdCallbackDto);

        long documentTaskId = automatedStitchingExecutor.startStitching(cdamDto, bundle.getValue());
        ccdCallbackDto.setDocumentTaskId(documentTaskId);

        return bundle;
    }

    private CcdValue<CcdBundleDTO> bundleJsonToBundleValue(JsonNode jsonNode) throws IOException {
        return objectMapper.readValue(objectMapper.treeAsTokens(jsonNode), type);
    }
}
