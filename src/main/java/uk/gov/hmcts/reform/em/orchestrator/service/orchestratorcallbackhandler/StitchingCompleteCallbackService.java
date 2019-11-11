package uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdapi.CcdDataApiCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdapi.CcdDataApiEventCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;

@Service
@Transactional
public class StitchingCompleteCallbackService {

    private final CcdDataApiEventCreator ccdDataApiEventCreator;
    private final CcdDataApiCaseUpdater ccdDataApiCaseUpdater;
    private final CcdCallbackBundleUpdater ccdCallbackBundleUpdater;

    public StitchingCompleteCallbackService(CcdDataApiEventCreator ccdDataApiEventCreator,
                                            CcdDataApiCaseUpdater ccdDataApiCaseUpdater,
                                            CcdCallbackBundleUpdater ccdCallbackBundleUpdater) {
        this.ccdDataApiEventCreator = ccdDataApiEventCreator;
        this.ccdDataApiCaseUpdater = ccdDataApiCaseUpdater;
        this.ccdCallbackBundleUpdater = ccdCallbackBundleUpdater;
    }

    public void handleCallback(StitchingCompleteCallbackDto stitchingCompleteCallbackDto) {

        CcdCallbackDto ccdCallbackDto = null;

        try {

            ccdCallbackDto = ccdDataApiEventCreator.executeTrigger(stitchingCompleteCallbackDto.getCaseId(),
                    stitchingCompleteCallbackDto.getTriggerId(),
                    stitchingCompleteCallbackDto.getJwt());

            ccdCallbackBundleUpdater.updateBundle(ccdCallbackDto, stitchingCompleteCallbackDto);

        } finally {
            if (ccdCallbackDto != null) {
                ccdDataApiCaseUpdater.executeUpdate(ccdCallbackDto, stitchingCompleteCallbackDto.getJwt());
            }
        }

    }

}
