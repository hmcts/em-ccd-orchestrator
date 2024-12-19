package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

class CcdCallbackDtoCreatorTest {

    ObjectMapper objectMapper = new ObjectMapper();

    CcdCallbackDtoCreator ccdCallbackDtoCreator = new CcdCallbackDtoCreator(objectMapper);

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(ccdCallbackDtoCreator, "enableCdamValidation", false);
    }

    @Test
    void createDto() throws Exception {

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");

        Mockito.when(mockRequest.getReader()).thenReturn(new BufferedReader(
            new StringReader("{\"case_details\":{\"case_data\": {\"a\":\"b\"}}}")));

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest);

        assertEquals("a", ccdCallbackDto.getJwt());
        assertEquals("b", ccdCallbackDto.getCaseData().at("/a").asText());
        assertEquals(Optional.of("caseBundles"), ccdCallbackDto.getPropertyName());
    }

    @Test
    void createDtoWithParameter() throws Exception {

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader()).thenReturn(
            new BufferedReader(new StringReader("{\"case_details\":{\"case_data\": {\"a\":\"b\"}}}")));

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest, "myProd");

        assertEquals("a", ccdCallbackDto.getJwt());

        assertEquals("b", ccdCallbackDto.getCaseData().at("/a").asText());

        assertEquals(Optional.of("myProd"), ccdCallbackDto.getPropertyName());
    }

    @Test
    void createDtoWithException() throws Exception {

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader()).thenThrow(new IOException("xxx"));

        assertThrows(CantReadCcdPayloadException.class, () -> ccdCallbackDtoCreator.createDto(mockRequest, "myProd"));
    }

    @Test
    void createDtoWithEmptyMessage() throws Exception {
        ObjectMapper mockMapper = Mockito.mock(ObjectMapper.class);
        CcdCallbackDtoCreator callbackDtoCreator = new CcdCallbackDtoCreator(mockMapper);

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader("")));

        assertThrows(CantReadCcdPayloadException.class, () -> callbackDtoCreator.createDto(mockRequest, "myProd"));
    }

    @Test
    void createDtoWithNull() throws Exception {
        ObjectMapper mockMapper = Mockito.mock(ObjectMapper.class);
        CcdCallbackDtoCreator callbackDtoCreator = new CcdCallbackDtoCreator(mockMapper);

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockMapper.readTree(any(JsonParser.class))).thenReturn(null);

        assertThrows(CantReadCcdPayloadException.class, () -> callbackDtoCreator.createDto(mockRequest, "myProd"));
    }

}
