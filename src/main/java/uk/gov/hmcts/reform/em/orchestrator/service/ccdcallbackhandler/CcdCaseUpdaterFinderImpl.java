package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdaterFinder;

import java.util.List;
import java.util.Optional;

@Service
public class CcdCaseUpdaterFinderImpl implements CcdCaseUpdaterFinder {

    private final List<CcdCaseUpdater> ccdCaseUpdaterList;

    public CcdCaseUpdaterFinderImpl(List<CcdCaseUpdater> ccdCaseUpdaterList) {
        this.ccdCaseUpdaterList = ccdCaseUpdaterList;
    }

    @Override
    public Optional<CcdCaseUpdater> find(CcdCallbackDto ccdCallbackDto) {
        return ccdCaseUpdaterList
                .stream()
                .filter(ccdCaseUpdater -> ccdCaseUpdater.handles(ccdCallbackDto))
                .findFirst();
    }
}
