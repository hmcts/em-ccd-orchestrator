package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;

@Service
@Transactional
public class CcdBundleStitchingService implements CcdCaseUpdater {

    @Override
    public boolean handles(CcdCallbackDto ccdCallbackDto) {
        return true;
    }

    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {
        return null;
    }

}
