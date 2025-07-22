package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingServiceException;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CdamDto;
import uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities;

import java.util.Set;

@Service
public class CcdBundleStitchingService extends UpdateCase {

    private final Validator validator;
    private final StitchingService stitchingService;

    public CcdBundleStitchingService(ObjectMapper objectMapper,
                                     StitchingService stitchingService,
                                     Validator validator) {
        super(objectMapper);
        this.stitchingService = stitchingService;
        this.validator = validator;
    }

    protected CcdValue<CcdBundleDTO> stitchBundle(CcdValue<CcdBundleDTO> bundle, CcdCallbackDto ccdCallbackDto) {
        bundle.getValue().setCoverpageTemplateData(ccdCallbackDto.getCaseDetails());
        ccdCallbackDto.setEnableEmailNotification(bundle.getValue().getEnableEmailNotificationAsBoolean());
        Set<ConstraintViolation<CcdBundleDTO>> violations = validator.validate(bundle.getValue());

        if (!violations.isEmpty()) {
            throw new InputValidationException(violations);
        }
        CdamDto cdamDto = StringUtilities.populateCdamDetails(ccdCallbackDto);
        try {

            CcdDocument stitchedDocumentURI = stitchingService.stitch(bundle.getValue(), cdamDto);
            bundle.getValue().setStitchedDocument(stitchedDocumentURI);

            return bundle;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new StitchingServiceException(cdamDto.getCaseId(), e.getMessage(), e);
        }
    }
}
