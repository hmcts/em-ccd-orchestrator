package uk.gov.hmcts.reform.em.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public class CcdCallbackDto {

    private Optional<String> propertyName;

    private JsonNode caseData;

    private String jwt;

    public JsonNode getCaseData() {
        return caseData;
    }

    public String getJwt() {
        return jwt;
    }

    public void setCaseData(JsonNode caseData) {
        this.caseData = caseData;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public Optional<String> getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(Optional<String> propertyName) {
        this.propertyName = propertyName;
    }
}
