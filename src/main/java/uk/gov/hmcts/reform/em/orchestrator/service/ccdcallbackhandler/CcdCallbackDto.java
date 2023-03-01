package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import pl.touk.throwing.ThrowingFunction;
import uk.gov.hmcts.reform.em.orchestrator.config.Constants;

import javax.validation.constraints.NotNull;
import java.util.Optional;

public class CcdCallbackDto {

    private Optional<String> propertyName = Optional.empty();

    private JsonNode ccdPayload;

    private JsonNode caseData;

    private JsonNode caseDetails;

    private String jwt;

    @Getter
    @Setter
    private String serviceAuth;

    @Getter
    @Setter
    private long documentTaskId;

    @JsonIgnore
    private Boolean enableEmailNotification;

    public JsonNode getCaseData() {
        return caseData;
    }

    public JsonNode getCaseDetails() {
        return caseDetails;
    }

    public String getJwt() {
        return jwt;
    }

    public void setCaseData(JsonNode caseData) {
        this.caseData = caseData;
    }

    public void setCaseDetails(JsonNode caseDetails) {
        this.caseDetails = caseDetails;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    @JsonIgnore
    public Boolean getEnableEmailNotification() {
        if (enableEmailNotification == null) {
            return false;
        }
        return enableEmailNotification;
    }

    @JsonIgnore
    public void setEnableEmailNotification(Boolean enableEmailNotification) {
        this.enableEmailNotification = enableEmailNotification;
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

    public JsonNode getCcdPayload() {
        return ccdPayload;
    }

    public void setCcdPayload(JsonNode ccdPayload) {
        this.ccdPayload = ccdPayload;
    }

    public String getCaseId() {
        return ccdPayload != null && ccdPayload.findValue("id") != null
                ? ccdPayload.findValue("id").asText() : null;
    }

    @NotNull(message = "jurisdictionId or jurisdiction is required attribute")
    public String getJurisdictionId() {
        if (getJurisdictionIdAttribute() != null) {
            return getJurisdictionIdAttribute();
        }
        return getJurisdiction();

    }

    public String getJurisdictionIdAttribute() {
        return ccdPayload != null && ccdPayload.findValue(Constants.JURISDICTION_ID) != null
                ? ccdPayload.findValue(Constants.JURISDICTION_ID).asText() : null;
    }

    public String getJurisdiction() {
        return ccdPayload != null && ccdPayload.findValue(Constants.JURISDICTION) != null
                ? ccdPayload.findValue(Constants.JURISDICTION).asText() : null;
    }

    @NotNull(message = "caseTypeId or case_type_id is required attribute")
    public String getCaseTypeId() {
        if (getPrimeCaseTypeId() != null) {
            return getPrimeCaseTypeId();
        }
        return getAlternativeCaseTypeId();

    }

    private String getPrimeCaseTypeId() {
        return ccdPayload != null && ccdPayload.findValue(Constants.CASE_TYPE_ID) != null
                ? ccdPayload.findValue(Constants.CASE_TYPE_ID).asText() : null;
    }

    private String getAlternativeCaseTypeId() {
        return ccdPayload != null && ccdPayload.findValue(Constants.ALT_CASE_TYPE_ID) != null
                ? ccdPayload.findValue(Constants.ALT_CASE_TYPE_ID).asText() : null;
    }

    public String getEventToken() {
        return ccdPayload != null && ccdPayload.findValue("token") != null
                ? ccdPayload.findValue("token").asText() : null;
    }

    public String getEventId() {
        return ccdPayload != null && ccdPayload.findValue("event_id") != null
                ? ccdPayload.findValue("event_id").asText() : null;
    }

    public String getIdentifierFromCcdPayload(String identifier) {
        if (identifier != null) {
            JsonNode value = ccdPayload.at(identifier);
            return value.isMissingNode() ? "" : value.asText();
        }
        return "";
    }
}
