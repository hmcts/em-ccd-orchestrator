package uk.gov.hmcts.reform.em.stitching.service.callback;

import com.fasterxml.jackson.databind.JsonNode;

public class CcdCallbackDto {

    private String propertyName;

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

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
