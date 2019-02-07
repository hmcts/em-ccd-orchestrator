package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdaterFinder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CcdCaseUpdaterFinderImpl implements CcdCaseUpdaterFinder {

    private final List<CcdCaseUpdater> ccdCaseUpdaterList;

    public CcdCaseUpdaterFinderImpl(List<CcdCaseUpdater> ccdCaseUpdaterList) {
        this.ccdCaseUpdaterList = ccdCaseUpdaterList;
    }

    @Override
    public Optional<CcdCaseUpdater> find(CcdCallbackDto ccdCallbackDto) {
        List<CcdCaseUpdater> handlingUpdaters = ccdCaseUpdaterList
                .stream()
                .filter(ccdCaseUpdater -> ccdCaseUpdater.handles(ccdCallbackDto))
                .collect(Collectors.toList());
        return handlingUpdaters.size() >= 0 ? Optional.of(handlingUpdaters.get(0)) : Optional.ofNullable(null);
    }
}
