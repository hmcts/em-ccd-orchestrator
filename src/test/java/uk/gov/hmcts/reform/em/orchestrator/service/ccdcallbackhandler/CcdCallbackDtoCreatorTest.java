package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

class CcdCallbackDtoCreatorTest {

    ObjectMapper objectMapper = new ObjectMapper();

    CcdCallbackDtoCreator ccdCallbackDtoCreator = new CcdCallbackDtoCreator(objectMapper);

    @BeforeEach
    void setup() {
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

    @Test
    void createDtoFromStartEventResponse() {
        ObjectMapper realMapper = new ObjectMapper();
        CcdCallbackDtoCreator creatorWithRealMapper = new CcdCallbackDtoCreator(realMapper);
        ReflectionTestUtils.setField(creatorWithRealMapper, "enableCdamValidation", false);


        StartEventResponse mockStartEventResponse = Mockito.mock(StartEventResponse.class);
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("field1", "value1");
        CaseDetails caseDetails = CaseDetails.builder().data(caseDataMap).build();
        Mockito.when(mockStartEventResponse.getCaseDetails()).thenReturn(caseDetails);

        CcdCallbackDto ccdCallbackDto = creatorWithRealMapper.createDto("testProperty",
            "testJwt", mockStartEventResponse);

        assertEquals("testJwt", ccdCallbackDto.getJwt());
        assertEquals(Optional.of("testProperty"), ccdCallbackDto.getPropertyName());
        assertEquals("value1", ccdCallbackDto.getCaseData().get("field1").asText());
        assertFalse(ccdCallbackDto.isEnableCdamValidation());
    }

    @Test
    void createDtoWhenSupplierThrowsRuntimeException() {
        ObjectMapper mockMapper = Mockito.mock(ObjectMapper.class);
        CcdCallbackDtoCreator callbackDtoCreatorWithMockMapper = new CcdCallbackDtoCreator(mockMapper);
        ReflectionTestUtils.setField(callbackDtoCreatorWithMockMapper, "enableCdamValidation", false);

        StartEventResponse mockStartEventResponse = Mockito.mock(StartEventResponse.class);

        Mockito.when(mockMapper.valueToTree(any(StartEventResponse.class)))
            .thenThrow(new RuntimeException("Simulated supplier error"));

        assertThrows(CantReadCcdPayloadException.class,
            () -> callbackDtoCreatorWithMockMapper.createDto("testProp", "testJwt", mockStartEventResponse));
    }

}