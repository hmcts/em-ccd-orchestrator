package uk.gov.hmcts.reform.em.stitching.service.callback;

import com.fasterxml.jackson.databind.JsonNode;

public interface CcdCallbackHandlerService {

    JsonNode handleCddCallback(CcdCallbackDto caseData, CcdCaseUpdater ccdCaseUpdater);

}
