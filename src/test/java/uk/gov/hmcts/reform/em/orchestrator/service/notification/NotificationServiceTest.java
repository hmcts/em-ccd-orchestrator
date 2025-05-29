package uk.gov.hmcts.reform.em.orchestrator.service.notification;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.em.orchestrator.config.HttpClientConfiguration;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {NotificationService.class, HttpClientConfiguration.class})
@TestPropertySource("classpath:application.yaml")
class NotificationServiceTest {

    @MockitoBean
    private NotificationClient notificationClient;

    private NotificationService notificationService;

    private AutoCloseable openMocks;

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    void sendEmailNotificationSuccessful() throws NotificationClientException {
        List<String> responses = new ArrayList<>();
        responses.add("{ \"id\": 1, \"email\": \"email@email.com\", \"forename\": \"test\", \"surname\": \"user\" }");
        OkHttpClient http = getMockHttpSuccess(responses);

        setUpNotificationClient(http);

        notificationService.sendEmailNotification(
            "string",
            "string",
            "string",
            "string",
            "string"
        );
        verify(notificationClient, times(1))
            .sendEmail(
                "string",
                "email@email.com",
                getPersonalisation(),
                "Email Notification: string");
    }

    @Test
    void sendEmailNotificationFailure() throws NotificationClientException {
        List<String> responses = new ArrayList<>();
        responses.add("{ \"id\": 1, \"email\": \"email@email.com\", \"forename\": \"test\", \"surname\": \"user\" }");
        OkHttpClient http = getMockHttpSuccess(responses);

        setUpNotificationClient(http);

        when(notificationClient.sendEmail(
            "string",
            "email@email.com",
            getPersonalisation(),
            "Email Notification: string"
        )).thenThrow(NotificationClientException.class);

        CallbackException exception = assertThrows(CallbackException.class, () ->
            notificationService.sendEmailNotification(
            "string",
            "string",
            "string",
            "string",
            "string"
        ));
        assertTrue(exception.getMessage().contains("NotificationClientException"));
    }

    @Test
    void getUserDetailsFailure() {
        List<String> responses = new ArrayList<>();
        responses.add("{ \"id\": 1, \"email\": \"email@email.com\", \"forename\": \"test\", \"surname\": \"user\" }");
        OkHttpClient http = getMockHttpFailures(responses);

        setUpNotificationClient(http);

        assertThrows(CallbackException.class, () -> notificationService.sendEmailNotification(
            "string",
            "string",
            "string",
            "string",
            "string"
        ));
    }

    @Test
    void getUserDetailsIOException() {
        OkHttpClient http = getMockHttpIOException();
        setUpNotificationClient(http);

        CallbackException exception = assertThrows(CallbackException.class, () ->
            notificationService.sendEmailNotification(
            "string",
            "string",
            "string",
            "string",
            "string"
        ));
        assertTrue(exception.getMessage().contains("IOException: Simulated network problem"));
    }


    public void setUpNotificationClient(OkHttpClient http) {
        notificationService = new NotificationService(notificationClient, http);
        ReflectionTestUtils.setField(notificationService, "idamBaseUrl", "http://localhost:4501");
    }

    public OkHttpClient getMockHttpSuccess(List<String> body) {
        Iterator<String> iterator = body.iterator();

        return new OkHttpClient
            .Builder()
            .addInterceptor(chain -> new Response.Builder()
                .body(ResponseBody.create(iterator.next(), MediaType.get("application/json")))
                .request(chain.request())
                .message("")
                .code(200)
                .protocol(Protocol.HTTP_2)
                .build())
            .build();
    }

    public OkHttpClient getMockHttpFailures(List<String> body) {
        Iterator<String> iterator = body.iterator();

        return new OkHttpClient
            .Builder()
            .addInterceptor(chain -> new Response.Builder()
                .body(ResponseBody.create(iterator.next(), MediaType.get("application/json")))
                .request(chain.request())
                .message("")
                .code(500)
                .protocol(Protocol.HTTP_2)
                .build())
            .build();
    }

    public OkHttpClient getMockHttpIOException() {
        return new OkHttpClient.Builder()
            .addInterceptor((Interceptor.Chain chain) -> {
                throw new IOException("Simulated network problem");
            })
            .build();
    }

    public HashMap<String, String> getPersonalisation() {
        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put("case_reference", "string");
        personalisation.put("bundle_name", "string");
        personalisation.put("system_error_message", "string");
        return personalisation;
    }
}