package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.em.orchestrator.service.notification.NotificationService;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.StitchingCompleteCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.StitchingCompleteCallbackService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.TaskState;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StitchingCompleteCallbackControllerTest {

    @Mock
    private StitchingCompleteCallbackService stitchingCompleteCallbackService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private StitchingCompleteCallbackController controller;

    private static final String JWT = "testauthorization";
    private static final String CASE_ID = "testcaseid";
    private static final String TRIGGER_ID = "testtriggerid";
    private static final String BUNDLE_ID = "testbundleid";
    private static final String SUCCESS_TEMPLATE_ID = "successTemplate";
    private static final String FAILURE_TEMPLATE_ID = "failureTemplate";
    private static final String BUNDLE_TITLE = "Test Bundle Title";
    private static final String FAILURE_DESCRIPTION = "It Broke";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "successTemplateId", SUCCESS_TEMPLATE_ID);
        ReflectionTestUtils.setField(controller, "failureTemplateId", FAILURE_TEMPLATE_ID);
        when(request.getHeader("authorization")).thenReturn(JWT);
    }

    private DocumentTaskDTO createDocumentTaskDTO(TaskState taskState,
                                                  Boolean enableEmailNotification, String failureDescription) {
        DocumentTaskDTO documentTaskDTO = new DocumentTaskDTO();
        documentTaskDTO.setTaskState(taskState);
        StitchingBundleDTO bundleDTO = new StitchingBundleDTO();
        bundleDTO.setBundleTitle(BUNDLE_TITLE);
        bundleDTO.setEnableEmailNotification(enableEmailNotification);
        documentTaskDTO.setBundle(bundleDTO);
        documentTaskDTO.setFailureDescription(failureDescription);
        return documentTaskDTO;
    }

    @Test
    void stitchingCompleteCallbackDoneAndEmailEnabled() throws CallbackException {
        DocumentTaskDTO documentTaskDTO = createDocumentTaskDTO(TaskState.DONE, true, null);

        ResponseEntity<CallbackException> response = controller.stitchingCompleteCallback(
            request, CASE_ID, TRIGGER_ID, BUNDLE_ID, documentTaskDTO
        );

        verify(stitchingCompleteCallbackService).handleCallback(any(StitchingCompleteCallbackDto.class));
        verify(notificationService).sendEmailNotification(
            SUCCESS_TEMPLATE_ID, JWT, CASE_ID, BUNDLE_TITLE, null
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void stitchingCompleteCallbackFailedAndEmailEnabled() throws CallbackException {
        DocumentTaskDTO documentTaskDTO = createDocumentTaskDTO(TaskState.FAILED, true, FAILURE_DESCRIPTION);

        ResponseEntity<CallbackException> response = controller.stitchingCompleteCallback(
            request, CASE_ID, TRIGGER_ID, BUNDLE_ID, documentTaskDTO
        );

        verify(stitchingCompleteCallbackService).handleCallback(any(StitchingCompleteCallbackDto.class));
        verify(notificationService).sendEmailNotification(
            FAILURE_TEMPLATE_ID, JWT, CASE_ID, BUNDLE_TITLE, FAILURE_DESCRIPTION
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void stitchingCompleteCallbackDoneAndEmailDisabled() throws CallbackException {
        DocumentTaskDTO documentTaskDTO = createDocumentTaskDTO(TaskState.DONE, false, null);

        ResponseEntity<CallbackException> response = controller.stitchingCompleteCallback(
            request, CASE_ID, TRIGGER_ID, BUNDLE_ID, documentTaskDTO
        );

        verify(stitchingCompleteCallbackService).handleCallback(any(StitchingCompleteCallbackDto.class));
        verify(notificationService, never()).sendEmailNotification(any(), any(), any(), any(), any());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void stitchingCompleteCallbackDoneAndEmailNull() throws CallbackException {
        DocumentTaskDTO documentTaskDTO = createDocumentTaskDTO(TaskState.DONE, null, null);

        ResponseEntity<CallbackException> response = controller.stitchingCompleteCallback(
            request, CASE_ID, TRIGGER_ID, BUNDLE_ID, documentTaskDTO
        );

        verify(stitchingCompleteCallbackService).handleCallback(any(StitchingCompleteCallbackDto.class));
        verify(notificationService, never()).sendEmailNotification(any(), any(), any(), any(), any());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void stitchingCompleteCallbackInProgressAndEmailEnabled() throws CallbackException {
        DocumentTaskDTO documentTaskDTO = createDocumentTaskDTO(TaskState.IN_PROGRESS, true, null);

        ResponseEntity<CallbackException> response = controller.stitchingCompleteCallback(
            request, CASE_ID, TRIGGER_ID, BUNDLE_ID, documentTaskDTO
        );

        verify(stitchingCompleteCallbackService).handleCallback(any(StitchingCompleteCallbackDto.class));
        verify(notificationService, never()).sendEmailNotification(any(), any(), any(), any(), any());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void stitchingCompleteCallbackServiceThrowsException() throws CallbackException {
        DocumentTaskDTO documentTaskDTO = createDocumentTaskDTO(TaskState.DONE, true, null);
        CallbackException callbackException = new CallbackException(400, "Error from service", "Details");
        doThrow(callbackException).when(stitchingCompleteCallbackService)
            .handleCallback(any(StitchingCompleteCallbackDto.class));

        ResponseEntity<CallbackException> response = controller.stitchingCompleteCallback(
            request, CASE_ID, TRIGGER_ID, BUNDLE_ID, documentTaskDTO
        );

        verify(notificationService, never()).sendEmailNotification(any(), any(), any(), any(), any());
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(callbackException, response.getBody());
    }

    @Test
    void stitchingCompleteCallbackNotificationServiceThrowsException() throws CallbackException {
        DocumentTaskDTO documentTaskDTO = createDocumentTaskDTO(TaskState.DONE, true, null);
        CallbackException notificationException = new CallbackException(503,
            "Error from notification", "Notify unavailable");
        doThrow(notificationException).when(notificationService).sendEmailNotification(
            SUCCESS_TEMPLATE_ID, JWT, CASE_ID, BUNDLE_TITLE, null
        );

        ResponseEntity<CallbackException> response = controller.stitchingCompleteCallback(
            request, CASE_ID, TRIGGER_ID, BUNDLE_ID, documentTaskDTO
        );

        verify(stitchingCompleteCallbackService).handleCallback(any(StitchingCompleteCallbackDto.class));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals(notificationException, response.getBody());
    }
}