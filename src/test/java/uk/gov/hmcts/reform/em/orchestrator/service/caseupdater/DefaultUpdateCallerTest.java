package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;
import uk.gov.hmcts.reform.em.orchestrator.service.notification.NotificationService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultUpdateCallerTest {

    @Mock
    private CcdCallbackDtoCreator ccdCallbackDtoCreator;

    @Mock
    private CcdCaseUpdater ccdCaseUpdater;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpSession httpSession;

    @Mock
    private NotificationService notificationService;

    private ObjectMapper objectMapper = new ObjectMapper();

    DefaultUpdateCaller defaultUpdateCaller;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        //Below is required to create validator object. As mocking of validator does not work.
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        defaultUpdateCaller = new DefaultUpdateCaller(ccdCallbackDtoCreator, notificationService, validator);
    }

    @Test
    public void executeUpdate() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode caseData = mock(JsonNode.class);
        ccdCallbackDto.setCcdPayload(caseData);

        when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
            .thenReturn(ccdCallbackDto);

        when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class)))
            .thenReturn(objectMapper.readTree("{ \"p\" : 1 }"));

        ResponseEntity<CcdCallbackResponseDto> response =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);
        Assert.assertEquals(200, response.getStatusCodeValue());
        CcdCallbackResponseDto ccdCallbackResponseDto = response.getBody();
        Assert.assertEquals(1, ccdCallbackResponseDto.getData().get("p").asInt());
        Mockito.verify(httpServletRequest, Mockito.times(0)).getSession();
    }

    @Test
    public void executeUpdateValidCdam() throws Exception {
        ReflectionTestUtils.setField(defaultUpdateCaller, "enableCdamValidation", true);
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ObjectNode caseData = objectMapper.createObjectNode();
        caseData.put("jurisdictionId", "jurisdictionId");
        caseData.put("caseTypeId", "caseTypeId");
        ccdCallbackDto.setCcdPayload(caseData);
        when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
                .thenReturn(ccdCallbackDto);

        when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class)))
                .thenReturn(objectMapper.readTree("{ \"p\" : 1 }"));

        ResponseEntity<CcdCallbackResponseDto> response =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);
        Assert.assertEquals(200, response.getStatusCodeValue());
        CcdCallbackResponseDto ccdCallbackResponseDto = response.getBody();
        Assert.assertNotNull(ccdCallbackResponseDto);
        Assert.assertEquals(1, ccdCallbackResponseDto.getData().get("p").asInt());
        Mockito.verify(httpServletRequest, Mockito.times(0)).getSession();
    }

    @Test
    public void executeUpdatePropertyNotFoundException() throws Exception {
        ReflectionTestUtils.setField(defaultUpdateCaller, "enableCdamValidation", true);
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode caseData = mock(JsonNode.class);
        ccdCallbackDto.setCcdPayload(caseData);
        when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
                .thenReturn(ccdCallbackDto);

        ResponseEntity<CcdCallbackResponseDto> response =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);
        Assert.assertEquals(400, response.getStatusCodeValue());
        CcdCallbackResponseDto ccdCallbackResponseDto = response.getBody();
        Assert.assertNotNull(ccdCallbackResponseDto);
        Assert.assertTrue(ccdCallbackResponseDto.getErrors().contains("caseTypeId or case_type_id is required attribute"));
        Assert.assertTrue(ccdCallbackResponseDto.getErrors().contains("jurisdictionId or jurisdiction is required attribute"));

    }

    @Test
    public void executeUpdateInputValidationException() {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode caseData = mock(JsonNode.class);
        ccdCallbackDto.setCcdPayload(caseData);
        ccdCallbackDto.setCaseData(caseData);
        ccdCallbackDto.setEnableEmailNotification(true);

        when(caseData.has(anyString())).thenReturn(true);
        when(caseData.get(anyString())).thenReturn(caseData);
        when(caseData.asText()).thenReturn("json value");

        when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
                .thenReturn(ccdCallbackDto);

        InputValidationException e = mock(InputValidationException.class);

        when(e.getViolations())
                .thenReturn(Stream.of("abc").collect(Collectors.toList()));

        when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class)))
                .thenThrow(e);
        ResponseEntity<CcdCallbackResponseDto> response =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);
        Assert.assertEquals(400, response.getStatusCodeValue());
        CcdCallbackResponseDto ccdCallbackResponseDto = response.getBody();

        Assert.assertEquals("abc", ccdCallbackResponseDto.getErrors().get(0));
    }

    @Test
    public void executeUpdateException() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode caseData = mock(JsonNode.class);
        ccdCallbackDto.setCcdPayload(caseData);
        ccdCallbackDto.setCaseData(caseData);
        ccdCallbackDto.setEnableEmailNotification(true);

        when(caseData.has(anyString())).thenReturn(true);
        when(caseData.get(anyString())).thenReturn(caseData);
        when(caseData.asText()).thenReturn("json value");

        when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
                .thenReturn(ccdCallbackDto);

        when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class)))
                .thenThrow(new RuntimeException("x"));

        ResponseEntity<CcdCallbackResponseDto> response =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);
        Assert.assertEquals(400, response.getStatusCodeValue());
        CcdCallbackResponseDto ccdCallbackResponseDto = response.getBody();
        Assert.assertEquals("x", ccdCallbackResponseDto.getErrors().get(0));
    }

    @Test
    public void executeUpdateExceptionWithNoEmailNotification() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode caseData = mock(JsonNode.class);
        ccdCallbackDto.setCcdPayload(caseData);
        ccdCallbackDto.setCaseData(caseData);
        ccdCallbackDto.setEnableEmailNotification(false);

        when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
                .thenReturn(ccdCallbackDto);

        when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class)))
                .thenThrow(new RuntimeException("x"));

        ResponseEntity<CcdCallbackResponseDto> response =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);
        Assert.assertEquals(400, response.getStatusCodeValue());

        CcdCallbackResponseDto ccdCallbackResponseDto = response.getBody();
        Assert.assertEquals("x", ccdCallbackResponseDto.getErrors().get(0));
    }

}
