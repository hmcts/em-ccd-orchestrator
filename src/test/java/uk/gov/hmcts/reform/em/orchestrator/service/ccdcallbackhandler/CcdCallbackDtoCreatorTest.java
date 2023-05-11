package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

public class CcdCallbackDtoCreatorTest {

    ObjectMapper objectMapper = new ObjectMapper();

    CcdCallbackDtoCreator ccdCallbackDtoCreator = new CcdCallbackDtoCreator(objectMapper);

    @Before
    public void setup() {
        ReflectionTestUtils.setField(ccdCallbackDtoCreator, "enableCdamValidation", false);
    }

    @Test
    public void createDto() throws Exception {

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");

        Mockito.when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader("{\"case_details\":{\"case_data\": {\"a\":\"b\"}}}")));

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest);

        Assert.assertEquals("a", ccdCallbackDto.getJwt());
        Assert.assertEquals("b", ccdCallbackDto.getCaseData().at("/a").asText());
        Assert.assertEquals(Optional.ofNullable("caseBundles"), ccdCallbackDto.getPropertyName());
    }

    @Test
    public void createDtoWithParameter() throws Exception {

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader("{\"case_details\":{\"case_data\": {\"a\":\"b\"}}}")));

        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(mockRequest, "myProd");

        Assert.assertEquals("a", ccdCallbackDto.getJwt());

        Assert.assertEquals("b", ccdCallbackDto.getCaseData().at("/a").asText());

        Assert.assertEquals(Optional.of("myProd"), ccdCallbackDto.getPropertyName());
    }

    @Test(expected = CantReadCcdPayloadException.class)
    public void createDtoWithException() throws Exception {

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader()).thenThrow(new IOException("xxx"));

        ccdCallbackDtoCreator.createDto(mockRequest, "myProd");
    }

    @Test(expected = CantReadCcdPayloadException.class)
    public void createDtoWithEmptyMessage() throws Exception {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        CcdCallbackDtoCreator ccdCallbackDtoCreator = new CcdCallbackDtoCreator(objectMapper);

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(mockRequest.getReader()).thenReturn(new BufferedReader(new StringReader("")));

        ccdCallbackDtoCreator.createDto(mockRequest, "myProd");
    }

    @Test(expected = CantReadCcdPayloadException.class)
    public void createDtoWithNull() throws Exception {
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        CcdCallbackDtoCreator ccdCallbackDtoCreator = new CcdCallbackDtoCreator(objectMapper);

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(mockRequest.getHeader("Authorization")).thenReturn("a");
        Mockito.when(objectMapper.readTree(any(JsonParser.class))).thenReturn(null);


        ccdCallbackDtoCreator.createDto(mockRequest, "myProd");
    }

}
