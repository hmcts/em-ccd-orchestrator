package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.databind.JsonNode;

public interface CcdCallbackHandlerService {

    JsonNode handleCcdCallback(CcdCallbackDto caseData);

}
