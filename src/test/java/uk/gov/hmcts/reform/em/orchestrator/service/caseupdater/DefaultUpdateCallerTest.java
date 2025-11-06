package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;
import uk.gov.hmcts.reform.em.orchestrator.service.notification.NotificationService;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultUpdateCallerTest {

    @Mock
    private CcdCallbackDtoCreator ccdCallbackDtoCreator;

    @Mock
    private CcdCaseUpdater ccdCaseUpdater;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private NotificationService notificationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    DefaultUpdateCaller defaultUpdateCaller;

    private AutoCloseable openMocks;

    private ValidatorFactory validatorFactory;

    @BeforeEach
    void setup() {
        openMocks = MockitoAnnotations.openMocks(this);
        //Below is required to create validator object. As mocking of validator does not work.
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        defaultUpdateCaller = new DefaultUpdateCaller(ccdCallbackDtoCreator, notificationService, validator);
        validatorFactory = factory;
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    void executeUpdate() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode caseData = mock(JsonNode.class);
        ccdCallbackDto.setCcdPayload(caseData);

        when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
            .thenReturn(ccdCallbackDto);

        when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class)))
            .thenReturn(objectMapper.readTree("{ \"p\" : 1 }"));

        ResponseEntity<CcdCallbackResponseDto> response =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);
        assertEquals(200, response.getStatusCode().value());
        CcdCallbackResponseDto ccdCallbackResponseDto = response.getBody();
        assertNotNull(ccdCallbackResponseDto);
        assertEquals(1, ccdCallbackResponseDto.getData().get("p").asInt());
        Mockito.verify(httpServletRequest, Mockito.times(0)).getSession();
    }

    @Test
    void executeUpdateValidCdam() throws Exception {
        ReflectionTestUtils.setField(defaultUpdateCaller, "enableCdamValidation", true);
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setEnableCdamValidation(true);
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
        assertEquals(200, response.getStatusCode().value());
        CcdCallbackResponseDto ccdCallbackResponseDto = response.getBody();
        assertNotNull(ccdCallbackResponseDto);
        assertEquals(1, ccdCallbackResponseDto.getData().get("p").asInt());
        Mockito.verify(httpServletRequest, Mockito.times(0)).getSession();
    }

    @Test
    void executeUpdatePropertyNotFoundException() {
        ReflectionTestUtils.setField(defaultUpdateCaller, "enableCdamValidation", true);
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setEnableCdamValidation(true);
        JsonNode caseData = mock(JsonNode.class);
        ccdCallbackDto.setCcdPayload(caseData);
        when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
                .thenReturn(ccdCallbackDto);

        ResponseEntity<CcdCallbackResponseDto> response =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);
        assertEquals(400, response.getStatusCode().value());
        CcdCallbackResponseDto ccdCallbackResponseDto = response.getBody();
        assertNotNull(ccdCallbackResponseDto);
        Assertions.assertTrue(ccdCallbackResponseDto.getErrors()
            .contains("caseTypeId or case_type_id is required attribute"));
        Assertions.assertTrue(ccdCallbackResponseDto.getErrors()
            .contains("jurisdictionId or jurisdiction is required attribute"));

    }

    @Test
    void executeUpdateInputValidationException() {
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
                .thenReturn(Stream.of("abc").toList());

        when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class)))
                .thenThrow(e);
        ResponseEntity<CcdCallbackResponseDto> response =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);
        assertEquals(400, response.getStatusCode().value());
        CcdCallbackResponseDto ccdCallbackResponseDto = response.getBody();
        assertNotNull(ccdCallbackResponseDto);
        assertEquals("abc", ccdCallbackResponseDto.getErrors().getFirst());
    }

    @Test
    void executeUpdateException() {
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
        assertEquals(400, response.getStatusCode().value());
        CcdCallbackResponseDto ccdCallbackResponseDto = response.getBody();
        assertNotNull(ccdCallbackResponseDto);
        assertEquals("x", ccdCallbackResponseDto.getErrors().getFirst());
    }

    @Test
    void executeUpdateExceptionWithNoEmailNotification() {
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
        assertEquals(400, response.getStatusCode().value());

        CcdCallbackResponseDto ccdCallbackResponseDto = response.getBody();
        assertNotNull(ccdCallbackResponseDto);
        assertEquals("x", ccdCallbackResponseDto.getErrors().getFirst());
    }

}
