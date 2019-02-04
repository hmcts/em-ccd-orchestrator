package uk.gov.hmcts.reform.em.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public interface CasePropertyFinder {

    Optional<JsonNode> findCaseProperty(JsonNode caseData, String propertyName);

}
