package uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdapi.CcdUpdateService;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;

@Service
@Transactional
public class StitchingCompleteCallbackService {

    private final Logger log = LoggerFactory.getLogger(StitchingCompleteCallbackService.class);

    private final CcdUpdateService ccdUpdateService;
    private final CcdCallbackBundleUpdater ccdCallbackBundleUpdater;

    public StitchingCompleteCallbackService(
            CcdUpdateService ccdUpdateService,
            CcdCallbackBundleUpdater ccdCallbackBundleUpdater
    ) {
        this.ccdUpdateService = ccdUpdateService;
        this.ccdCallbackBundleUpdater = ccdCallbackBundleUpdater;
    }

    public void handleCallback(StitchingCompleteCallbackDto stitchingCompleteCallbackDto) {
        CcdCallbackDto ccdCallbackDto = null;

        ccdCallbackDto = ccdUpdateService.startCcdEvent(stitchingCompleteCallbackDto.getCaseId(),
                stitchingCompleteCallbackDto.getTriggerId(),
                stitchingCompleteCallbackDto.getJwt());

        ccdCallbackBundleUpdater.updateBundle(ccdCallbackDto, stitchingCompleteCallbackDto);

        ccdUpdateService.submitCcdEvent(stitchingCompleteCallbackDto.getCaseId(), stitchingCompleteCallbackDto.getJwt(), ccdCallbackDto);

        CaseDetails caseDetails = ccdUpdateService.getCaseDetails(stitchingCompleteCallbackDto.getJwt(),
                stitchingCompleteCallbackDto.getDocumentTaskDTO().getServiceAuth(),
                "a9ab7f4b-7e0c-49d4-8ed3-75b54d421cdc", "SSCS", "Benefit",
                "1602753891919320"
                );
        log.info("case Details are : {} ", caseDetails);
    }

}
