package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.em.orchestrator.config.JacksonGoldenMappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Context-independent leak guard for CCD callback responses on Jackson 3.
 * Complements MockMvc int tests: {@code copyOfCcdData} is {@code @JsonIgnore} and must never appear.
 */
class CcdCallbackResponseJacksonLeakTest {

    private final ObjectMapper mapper = JacksonGoldenMappers.bootLikeJsonMapper();

    @Test
    @DisplayName("CcdCallbackResponseDto does not serialize @JsonIgnore copyOfCcdData")
    void doesNotLeakCopyOfCcdData() throws Exception {
        JsonNode caseData = mapper.readTree("{\"case_field\":\"value\"}");
        CcdCallbackResponseDto response = new CcdCallbackResponseDto(caseData);
        response.setErrors(List.of());
        response.setWarnings(List.of());
        response.setDocumentTaskId(42L);

        String json = mapper.writeValueAsString(response);

        assertThat(json).contains("\"documentTaskId\":42");
        assertThat(json).contains("\"errors\":[]");
        assertThat(json).doesNotContain("copyOfCcdData");
        assertThat(json).doesNotContain("\"jwt\"");
    }
}
