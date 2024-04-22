package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedStitchingExecutor;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CdamDto;
import uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

import static pl.touk.throwing.ThrowingFunction.unchecked;

@Service
@Transactional
public class AsyncCcdBundleStitchingService extends UpdateCase {

    private final Validator validator;
    private final AutomatedStitchingExecutor automatedStitchingExecutor;

    public AsyncCcdBundleStitchingService(AutomatedStitchingExecutor automatedStitchingExecutor,
                                          Validator validator) {
        super(new ObjectMapper());
        this.automatedStitchingExecutor = automatedStitchingExecutor;
        this.validator = validator;
    }

    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {
        return super.updateCase(ccdCallbackDto);
    }

    @Override
    protected CcdValue<CcdBundleDTO> stitchBundle(CcdValue<CcdBundleDTO> bundle,
                                                CcdCallbackDto ccdCallbackDto) {
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
}
