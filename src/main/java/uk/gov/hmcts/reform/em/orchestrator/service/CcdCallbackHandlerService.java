package uk.gov.hmcts.reform.em.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdCallbackDto;

public interface CcdCallbackHandlerService {

    JsonNode handleCddCallback(CcdCallbackDto caseData, CcdCaseUpdater ccdCaseUpdater);

}
