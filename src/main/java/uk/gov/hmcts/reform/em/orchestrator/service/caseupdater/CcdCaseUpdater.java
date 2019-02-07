package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;

public interface CcdCaseUpdater {

    boolean handles(CcdCallbackDto ccdCallbackDto);

    JsonNode updateCase(CcdCallbackDto ccdCallbackDto);

}
