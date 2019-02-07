package uk.gov.hmcts.reform.em.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdCallbackDTO;

public interface CcdCallbackHandlerService {

    JsonNode handleCddCallback(CcdCallbackDTO caseData, CcdCaseUpdater ccdCaseUpdater);

}
