package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;

public interface CcdCaseUpdater {

    JsonNode updateCase(CcdCallbackDto ccdCallbackDto);

}
