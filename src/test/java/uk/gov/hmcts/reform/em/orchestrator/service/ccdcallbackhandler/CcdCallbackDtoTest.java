package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class CcdCallbackDtoTest {

    @Test
    public void getCaseIdNull() {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        assertNull(ccdCallbackDto.getCaseId());
    }

    @Test
    public void getCaseIdHappyPath() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode ccdPayload = objectMapper.readTree("{\"ccdPayload\": { \"id\": \"1\" }}");
        ccdCallbackDto.setCcdPayload(ccdPayload);
        assertEquals("1", ccdCallbackDto.getCaseId());

    }
}
