package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.ToString;

@ToString
public class CcdCaseDataContent {
    private CcdEvent event;
    private JsonNode data;

    // full event data payload
    @JsonProperty("event_data")
    private JsonNode eventData;

    @JsonProperty("security_classification")
    private String securityClassification;

    @JsonProperty("data_classification")
    private JsonNode dataClassification;

    @JsonProperty("event_token")
    private String token;

    @JsonProperty("ignore_warning")
    private Boolean ignoreWarning;

    @JsonProperty("draft_id")
    private String draftId;

    @JsonProperty("case_reference")
    private String caseReference;

    public CcdEvent getEvent() {
        return event;
    }

    @JsonIgnore
    public String getEventId() {
        return event.getEventId();
    }

    public void setEvent(CcdEvent event) {
        this.event = event;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }

    public JsonNode getEventData() {
        return eventData;
    }

    public void setEventData(JsonNode eventData) {
        this.eventData = eventData;
    }

    public String getSecurityClassification() {
        return securityClassification;
    }

    public void setSecurityClassification(String securityClassification) {
        this.securityClassification = securityClassification;
    }

    public JsonNode getDataClassification() {
        return dataClassification;
    }

    public void setDataClassification(JsonNode dataClassification) {
        this.dataClassification = dataClassification;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getIgnoreWarning() {
        return ignoreWarning == null ? Boolean.FALSE : ignoreWarning;
    }

    public void setIgnoreWarning(Boolean ignoreWarning) {
        this.ignoreWarning = ignoreWarning;
    }

    public String getDraftId() {
        return draftId;
    }

    public void setDraftId(String draftId) {
        this.draftId = draftId;
    }

    public String getCaseReference() {
        return caseReference;
    }

    public void setCaseReference(String caseReference) {
        this.caseReference = caseReference;
    }
}
