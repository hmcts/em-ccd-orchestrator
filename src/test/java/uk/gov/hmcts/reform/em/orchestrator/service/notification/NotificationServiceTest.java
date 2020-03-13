package uk.gov.hmcts.reform.em.orchestrator.service.notification;

import okhttp3.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.Instant;
import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class NotificationServiceTest {

    @MockBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private WebApplicationContext context;

    private static final String ID_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9"
            + ".eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsIm"
            + "p0aSI6ImQzNWRmMTRkLTA5ZjYtNDhmZi04YTkzLTdjNmYwMzM5MzE1OSIsImlhdCI6MTU0M"
            + "Tk3MTU4MywiZXhwIjoxNTQxOTc1MTgzfQ.QaQOarmV8xEUYV7yvWzX3cUE_4W1luMcWCwpr"
            + "oqqUrg";

    protected MockMvc restLogoutMockMvc;

    @Before
    public void setupMocks() {

        Map<String, Object> claims = new HashMap<>();
        claims.put("groups", "ROLE_USER");
        claims.put("sub", 123);

        OidcIdToken idToken = new OidcIdToken(ID_TOKEN, Instant.now(),
                Instant.now().plusSeconds(60), claims);

        SecurityContextHolder.getContext().setAuthentication(authenticationToken(idToken));

        SecurityContextHolderAwareRequestFilter authInjector = new SecurityContextHolderAwareRequestFilter();
        this.restLogoutMockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    private OAuth2AuthenticationToken authenticationToken(OidcIdToken idToken) {

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("USER"));
        OidcUser user = new DefaultOidcUser(authorities, idToken);

        return new OAuth2AuthenticationToken(user, authorities, "oidc");

    }

    @Mock
    private NotificationClient notificationClient;

    private NotificationService notificationService;

    @Test
    public void sendEmailNotificationSuccessful() throws NotificationClientException {
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

    @Test(expected = CallbackException.class)
    public void sendEmailNotificationFailure() throws NotificationClientException {
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

        notificationService.sendEmailNotification(
                "string",
                "string",
                "string",
                "string",
                "string"
        );
    }

    @Test(expected = CallbackException.class)
    public void getUserDetailsFailure() throws NotificationClientException {
        List<String> responses = new ArrayList<>();
        responses.add("{ \"id\": 1, \"email\": \"email@email.com\", \"forename\": \"test\", \"surname\": \"user\" }");
        OkHttpClient http = getMockHttpFailures(responses);

        setUpNotificationClient(http);

        notificationService.sendEmailNotification(
                "string",
                "string",
                "string",
                "string",
                "string"
        );
    }

    public void setUpNotificationClient(OkHttpClient http) {
        MockitoAnnotations.initMocks(this);
        notificationService = new NotificationService(notificationClient, http);
        ReflectionTestUtils.setField(notificationService, "idamBaseUrl", "http://localhost:4501");
    }

    public OkHttpClient getMockHttpSuccess(List<String> body) {
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

    public OkHttpClient getMockHttpFailures(List<String> body) {
        Iterator<String> iterator = body.iterator();

        return new OkHttpClient
                .Builder()
                .addInterceptor(chain -> new Response.Builder()
                        .body(ResponseBody.create(MediaType.get("application/json"), iterator.next()))
                        .request(chain.request())
                        .message("")
                        .code(500)
                        .protocol(Protocol.HTTP_2)
                        .build())
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
