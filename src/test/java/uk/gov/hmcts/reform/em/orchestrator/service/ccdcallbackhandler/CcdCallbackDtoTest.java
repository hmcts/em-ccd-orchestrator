package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

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
}
