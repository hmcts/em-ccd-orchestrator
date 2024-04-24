package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional
public class CcdBundleStitchingService extends UpdateCase {

    private final Logger logger = LoggerFactory.getLogger(CcdBundleStitchingService.class);

    private final Validator validator;
    private final StitchingService stitchingService;

    public CcdBundleStitchingService(ObjectMapper objectMapper,
                                     StitchingService stitchingService,
                                     Validator validator) {
        super(objectMapper);
        this.stitchingService = stitchingService;
        this.validator = validator;
    }

    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {
        return super.updateCase(ccdCallbackDto);
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
            logger.error(String.format("Stitching Failed for caseId : %s with issue : %s ",
                    StringUtilities.convertValidLog(cdamDto.getCaseId()),
                    StringUtilities.convertValidLog(e.getMessage())));
            throw new StitchingServiceException(e.getMessage(), e);
        }
    }
}
