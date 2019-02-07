package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.databind.JsonNode;
import pl.touk.throwing.ThrowingFunction;

import java.util.Optional;

public class CcdCallbackDto {

    private Optional<String> propertyName = Optional.empty();

    private JsonNode ccdPaylod;

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

    public Optional<JsonNode> findCaseProperty() {
        return propertyName.map(caseData::findValue);
    }

    public <T> Optional<T> findCaseProperty(Class<T> jsonSubclass) {
        return findCaseProperty().map(ThrowingFunction.unchecked(jsonSubclass::cast));
    }

    public JsonNode getCcdPaylod() {
        return ccdPaylod;
    }

    public void setCcdPaylod(JsonNode ccdPaylod) {
        this.ccdPaylod = ccdPaylod;
    }
}
