package uk.gov.hmcts.reform.em.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface CcdCaseUpdater {

    void updateCase(JsonNode caseData, JsonNode propertyData, String jwt);

}
