package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;

import java.util.Optional;

public interface CcdCaseUpdaterFinder {

    Optional<CcdCaseUpdater> find(CcdCallbackDto ccdCallbackDto);

}
