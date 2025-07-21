package uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdapi.CcdUpdateService;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;

@Service
public class StitchingCompleteCallbackService {

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

        ccdUpdateService.submitCcdEvent(stitchingCompleteCallbackDto.getCaseId(),
            stitchingCompleteCallbackDto.getJwt(), ccdCallbackDto);
    }

}
