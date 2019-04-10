package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdaterFinder;

@Service
@Transactional
public class CcdCallbackHandlerServiceImpl implements CcdCallbackHandlerService {

    private final CcdCaseUpdaterFinder ccdCaseUpdaterFinder;

    public CcdCallbackHandlerServiceImpl(CcdCaseUpdaterFinder ccdCaseUpdaterFinder) {
        this.ccdCaseUpdaterFinder = ccdCaseUpdaterFinder;
    }

    @Override
    public JsonNode handleCcdCallback(CcdCallbackDto ccdCallbackDto) {
        return ccdCaseUpdaterFinder
            .find(ccdCallbackDto)
            .map(ccdCaseUpdater -> ccdCaseUpdater.updateCase(ccdCallbackDto))
            .orElseThrow(() -> new CaseUpdaterDoesNotExistException("CaseUpdater does not exists for this case type"));
    }

}
