package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedStitchingExecutor;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CdamDto;
import uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities;

import java.util.Set;

@Service
public class AsyncCcdBundleStitchingService extends UpdateCase {

    private final Validator validator;
    private final AutomatedStitchingExecutor automatedStitchingExecutor;

    public AsyncCcdBundleStitchingService(ObjectMapper objectMapper,
                                          AutomatedStitchingExecutor automatedStitchingExecutor,
                                          Validator validator) {
        super(objectMapper);
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
