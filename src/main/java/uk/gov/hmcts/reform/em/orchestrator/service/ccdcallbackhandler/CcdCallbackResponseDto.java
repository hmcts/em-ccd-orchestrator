package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class CcdCallbackResponseDto {

    @JsonIgnore
    private JsonNode copyOfCcdData;

    public CcdCallbackResponseDto() {
    }

    public CcdCallbackResponseDto(JsonNode caseData) {
        this.copyOfCcdData = caseData != null ? caseData.deepCopy() : null;
        setData(caseData);
    }

    private JsonNode data;

    private List<String> errors = new ArrayList<>();

    private List<String> warnings = new ArrayList<>();

    @Getter
    @Setter
    private long documentTaskId;

    public JsonNode getData() {
        return CollectionUtils.isNotEmpty(getErrors()) ?  copyOfCcdData : data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
