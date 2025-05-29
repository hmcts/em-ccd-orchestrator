package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CcdCallbackDtoTest {

    @Test
    void getCaseIdNull() {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        assertNull(ccdCallbackDto.getCaseId());
    }

    @Test
    void getEnableEmailNotificationNull() {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        assertFalse(ccdCallbackDto.getEnableEmailNotification());
    }

    @Test
    void getPayloadStaticProperties() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode ccdPayload = objectMapper.readTree("{\"ccdPayload\": { \"id\": \"1\", "
            + "\"jurisdiction\": \"j\", \"caseTypeId\": \"c\", \"token\": \"t\", \"event_id\":\"e\" }}");
        ccdCallbackDto.setCcdPayload(ccdPayload);
        assertEquals("1", ccdCallbackDto.getCaseId());
        assertEquals("j", ccdCallbackDto.getJurisdiction());
        assertEquals("c", ccdCallbackDto.getCaseTypeId());
        assertEquals("t", ccdCallbackDto.getEventToken());
        assertEquals("e", ccdCallbackDto.getEventId());
        assertEquals("1", ccdCallbackDto.getIdentifierFromCcdPayload("/ccdPayload/id"));
    }

    @Test
    void getInvalidIdentifier() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode ccdPayload = objectMapper.readTree("{\"ccdPayload\": { \"id\": \"1\", "
            + "\"jurisdiction\": \"j\", \"case_type_id\": \"c\", \"token\": \"t\", \"event_id\":\"e\" }}");
        ccdCallbackDto.setCcdPayload(ccdPayload);
        assertEquals("", ccdCallbackDto.getIdentifierFromCcdPayload("/ccdPayload/idea"));
    }

    @Test
    void returnEmptyForNullIdentifier() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode ccdPayload = objectMapper.readTree("{\"ccdPayload\": { \"id\": \"1\", "
            + "\"jurisdiction\": \"j\", \"case_type_id\": \"c\", \"token\": \"t\", \"event_id\":\"e\" }}");
        ccdCallbackDto.setCcdPayload(ccdPayload);
        assertEquals("", ccdCallbackDto.getIdentifierFromCcdPayload(null));
    }

    @Test
    void getEnableEmailNotificationTrue() {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setEnableEmailNotification(true);
        assertTrue(ccdCallbackDto.getEnableEmailNotification());
    }

    @Test
    void getEnableEmailNotificationFalse() {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setEnableEmailNotification(false);
        assertFalse(ccdCallbackDto.getEnableEmailNotification());
    }

    @Test
    void getCaseIdMissingInPayload() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode ccdPayload = objectMapper.readTree("{\"ccdPayload\": { \"someOtherField\": \"value\" }}");
        ccdCallbackDto.setCcdPayload(ccdPayload);
        assertNull(ccdCallbackDto.getCaseId());
    }

    @Test
    void getEventTokenCcdPayloadNull() {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        assertNull(ccdCallbackDto.getEventToken());
    }

    @Test
    void getEventTokenMissingInPayload() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode ccdPayload = objectMapper.readTree("{\"ccdPayload\": { \"someOtherField\": \"value\" }}");
        ccdCallbackDto.setCcdPayload(ccdPayload);
        assertNull(ccdCallbackDto.getEventToken());
    }

    @Test
    void getEventIdCcdPayloadNull() {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        assertNull(ccdCallbackDto.getEventId());
    }

    @Test
    void getEventIdMissingInPayload() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode ccdPayload = objectMapper.readTree("{\"ccdPayload\": { \"someOtherField\": \"value\" }}");
        ccdCallbackDto.setCcdPayload(ccdPayload);
        assertNull(ccdCallbackDto.getEventId());
    }

    @ParameterizedTest
    @CsvSource({
        "true,  '{\"jurisdictionId\": \"jid_val\", \"jurisdiction\": \"j_val\"}', jid_val",
        "true,  '{\"jurisdiction\": \"j_val\"}', j_val",
        "true,  '{\"otherField\": \"value\"}', null",
        "true,  '', null",
        "false, '{\"jurisdictionId\": \"jid_val\", \"jurisdiction\": \"j_val\"}', jid_val",
        "false, '{\"jurisdiction\": \"j_val\"}', null",
        "false, '{\"otherField\": \"value\"}', null",
        "false, '', null"
    })
    void testGetJurisdictionId(boolean enableCdamValidation,
                               String ccdPayloadFieldsJson, String expectedJurisdictionId) throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setEnableCdamValidation(enableCdamValidation);
        ObjectMapper objectMapper = new ObjectMapper();

        if (Objects.nonNull(ccdPayloadFieldsJson) && !ccdPayloadFieldsJson.isBlank()) {
            JsonNode ccdPayload = objectMapper.readTree("{\"ccdPayload\": " + ccdPayloadFieldsJson + "}");
            ccdCallbackDto.setCcdPayload(ccdPayload);
        } else {
            ccdCallbackDto.setCcdPayload(null);
        }

        String expected = "null".equalsIgnoreCase(expectedJurisdictionId) ? null : expectedJurisdictionId;

        assertEquals(expected, ccdCallbackDto.getJurisdictionId());
    }

    @ParameterizedTest
    @CsvSource({
        "true,  '{\"caseTypeId\": \"p_ctid\", \"case_type_id\": \"a_ctid\"}', p_ctid",
        "true,  '{\"case_type_id\": \"a_ctid\"}', a_ctid",
        "true,  '{\"otherField\": \"value\"}', null",
        "true,  '', null",
        "false, '{\"caseTypeId\": \"p_ctid\", \"case_type_id\": \"a_ctid\"}', p_ctid",
        "false, '{\"case_type_id\": \"a_ctid\"}', null",
        "false, '{\"otherField\": \"value\"}', null",
        "false, '', null"
    })
    void testGetCaseTypeId(boolean enableCdamValidation,
                           String ccdPayloadFieldsJson, String expectedCaseTypeId) throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setEnableCdamValidation(enableCdamValidation);
        ObjectMapper objectMapper = new ObjectMapper();

        if (Objects.nonNull(ccdPayloadFieldsJson) && !ccdPayloadFieldsJson.isBlank()) {
            JsonNode ccdPayload = objectMapper.readTree("{\"ccdPayload\": " + ccdPayloadFieldsJson + "}");
            ccdCallbackDto.setCcdPayload(ccdPayload);
        } else {
            ccdCallbackDto.setCcdPayload(null);
        }

        String expected = "null".equalsIgnoreCase(expectedCaseTypeId) ? null : expectedCaseTypeId;

        assertEquals(expected, ccdCallbackDto.getCaseTypeId());
    }
}