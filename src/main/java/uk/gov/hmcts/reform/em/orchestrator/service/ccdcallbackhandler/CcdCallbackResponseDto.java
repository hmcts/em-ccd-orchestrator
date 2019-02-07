package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class CcdCallbackResponseDto {

    public CcdCallbackResponseDto() {
    }

    public CcdCallbackResponseDto(JsonNode data) {
        setData(data);
    }

    private JsonNode data;

    private List<String> errors;

    private List<String> warnings;

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }
}
