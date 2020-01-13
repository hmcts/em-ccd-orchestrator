package uk.gov.hmcts.reform.em.orchestrator.service.notification;

import okhttp3.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class NotificationServiceTest {

    @Mock
    private NotificationClient notificationClient;

    private NotificationService notificationService;

    @Before
    public void setUp() {
        List<String> responses = new ArrayList<>();
        responses.add("{ \"id\": 1, \"email\": \"email@email.com\", \"forename\": \"test\", \"surname\": \"user\" }");
        OkHttpClient http = getMockHttp(responses);

        MockitoAnnotations.initMocks(this);
        notificationService = new NotificationService(notificationClient, http);
        ReflectionTestUtils.setField(notificationService, "idamBaseUrl", "http://localhost:4501");
    }

    @Test
    public void sendEmailNotificationSuccessful() throws NotificationClientException {
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

    @Test(expected = CallbackException.class)
    public void sendEmailNotificationFailure() throws NotificationClientException {
        when(notificationClient.sendEmail(
                "string",
                "email@email.com",
                getPersonalisation(),
                "Email Notification: string"
        )).thenThrow(NotificationClientException.class);

        notificationService.sendEmailNotification(
                "string",
                "string",
                "string",
                "string",
                "string"
        );
    }

    public HashMap<String, String> getPersonalisation() {
        HashMap<String, String> personalisation = new HashMap<>();
        personalisation.put("case_reference", "string");
        personalisation.put("bundle_name", "string");
        personalisation.put("system_error_message", "string");
        return personalisation;
    }

    public OkHttpClient getMockHttp(List<String> body) {
        Iterator<String> iterator = body.iterator();

        return new OkHttpClient
                .Builder()
                .addInterceptor(chain -> new Response.Builder()
                        .body(ResponseBody.create(MediaType.get("application/json"), iterator.next()))
                        .request(chain.request())
                        .message("")
                        .code(200)
                        .protocol(Protocol.HTTP_2)
                        .build())
                .build();
    }
}
