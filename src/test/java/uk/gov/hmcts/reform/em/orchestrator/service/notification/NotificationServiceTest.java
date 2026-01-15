package uk.gov.hmcts.reform.em.orchestrator.service.notification;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationClient notificationClient;

    private NotificationService notificationService;

    @Captor
    private ArgumentCaptor<Map<String, String>> personalizationCaptor;

    private static final String IDAM_BASE_URL = "http://localhost:4501";
    private static final String JWT = "Bearer test-token";
    private static final String TEMPLATE_ID = "template-id-123";
    private static final String CASE_ID = "case-123";
    private static final String BUNDLE_TITLE = "My Bundle";
    private static final String FAILURE_MSG = "Something went wrong";
    private static final String EMAIL = "email@email.com";

    @Test
    void sendEmailNotificationSuccessful() throws Exception {
        String successBody = "{ \"id\": 1, \"email\": \"" + EMAIL
            + "\", \"forename\": \"test\", \"surname\": \"user\" }";

        setUpServiceWithResponse(200, successBody);

        notificationService.sendEmailNotification(TEMPLATE_ID, JWT, CASE_ID, BUNDLE_TITLE, FAILURE_MSG);

        verify(notificationClient).sendEmail(
            eq(TEMPLATE_ID),
            eq(EMAIL),
            personalizationCaptor.capture(),
            eq("Email Notification: " + CASE_ID)
        );

        Map<String, String> p = personalizationCaptor.getValue();
        assertEquals(CASE_ID, p.get("case_reference"));
        assertEquals(BUNDLE_TITLE, p.get("bundle_name"));
        assertEquals(FAILURE_MSG, p.get("system_error_message"));
    }

    @Test
    void sendEmailNotificationIdamFailure() {
        setUpServiceWithResponse(500, "Internal Server Error");

        CallbackException ex = assertThrows(CallbackException.class, () ->
            notificationService.sendEmailNotification(TEMPLATE_ID, JWT, CASE_ID, BUNDLE_TITLE, FAILURE_MSG)
        );

        assertEquals(500, ex.getHttpStatus());
        assertEquals("Unable to retrieve user details", ex.getMessage());
    }

    @Test
    void sendEmailNotificationNetworkIOException() {
        setUpServiceWithNetworkFailure();

        CallbackException ex = assertThrows(CallbackException.class, () ->
            notificationService.sendEmailNotification(TEMPLATE_ID, JWT, CASE_ID, BUNDLE_TITLE, FAILURE_MSG)
        );

        assertEquals(500, ex.getHttpStatus());
        assertTrue(ex.getMessage().contains("IOException"));
    }

    @Test
    void sendEmailNotificationNotifyClientException() throws Exception {
        String successBody = "{ \"email\": \"" + EMAIL + "\" }";
        setUpServiceWithResponse(200, successBody);

        doThrow(new NotificationClientException("GovNotify Error"))
            .when(notificationClient)
            .sendEmail(anyString(), anyString(), any(), anyString());

        CallbackException ex = assertThrows(CallbackException.class, () ->
            notificationService.sendEmailNotification(TEMPLATE_ID, JWT, CASE_ID, BUNDLE_TITLE, FAILURE_MSG)
        );

        assertTrue(ex.getMessage().contains("NotificationClientException"));
    }

    private void setUpServiceWithResponse(int statusCode, String jsonBody) {
        OkHttpClient mockHttp = new OkHttpClient.Builder()
            .addInterceptor(chain -> new Response.Builder()
                .request(chain.request())
                .protocol(Protocol.HTTP_1_1)
                .code(statusCode)
                .message("Mock Message")
                .body(ResponseBody.create(jsonBody, MediaType.get("application/json")))
                .build())
            .build();

        notificationService = new NotificationService(notificationClient, mockHttp);
        ReflectionTestUtils.setField(notificationService, "idamBaseUrl", IDAM_BASE_URL);
    }

    private void setUpServiceWithNetworkFailure() {
        OkHttpClient mockHttp = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                throw new IOException("Simulated network problem");
            })
            .build();

        notificationService = new NotificationService(notificationClient, mockHttp);
        ReflectionTestUtils.setField(notificationService, "idamBaseUrl", IDAM_BASE_URL);
    }
}