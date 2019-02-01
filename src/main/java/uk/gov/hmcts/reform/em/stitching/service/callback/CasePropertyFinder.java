package uk.gov.hmcts.reform.em.stitching.service.callback;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public interface CasePropertyFinder {

    Optional<JsonNode> findCaseProperty(JsonNode caseData, String propertyName);

}
