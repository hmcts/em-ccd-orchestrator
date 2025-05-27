package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.service.notification.NotificationService;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.StitchingCompleteCallbackService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.TaskState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = {Application.class})
@AutoConfigureMockMvc
class StitchingCompleteCallbackControllerTest extends BaseTest {

    @MockitoBean
    private StitchingCompleteCallbackService stitchingCompleteCallbackService;

    @MockitoBean
    private NotificationService notificationService;

    private String requestBody;

    @BeforeEach
    void setUp() throws IOException {

        MockitoAnnotations.openMocks(this);

        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        DocumentTaskDTO documentTaskDTO = new DocumentTaskDTO();
        StitchingBundleDTO stitchingBundleDTO = new StitchingBundleDTO();
        stitchingBundleDTO.setEnableEmailNotification(true);
        documentTaskDTO.setTaskState(TaskState.DONE);
        documentTaskDTO.setBundle(stitchingBundleDTO);

        ObjectMapper objectMapper = new ObjectMapper();
        requestBody = objectMapper.writeValueAsString(documentTaskDTO);
    }

    @Test
    void stitchingCompleteCallback() throws Exception {

        Mockito
            .doNothing()
            .when(notificationService)
            .sendEmailNotification(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString());

        mockMvc
            .perform(post("/api/stitching-complete-callback/abc/def/" + UUID.randomUUID())
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "xxx"))
            .andDo(print()).andExpect(status().isOk());

    }

    @Test
    void stitchingCompleteCallbackWithException() throws Exception {

        Mockito.doThrow(new CallbackException(456, "error", "error message"))
            .when(stitchingCompleteCallbackService).handleCallback(Mockito.any());

        mockMvc
            .perform(post("/api/stitching-complete-callback/abc/def/" + UUID.randomUUID())
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "xxx"))
            .andDo(print())
            .andExpect(status().is(456))
            .andExpect(jsonPath("$.message", Matchers.is("error message")))
            .andExpect(jsonPath("$.httpResponseBody", Matchers.is("error")));

    }

    private OAuth2AuthenticationToken authenticationToken(OidcIdToken idToken) {

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("USER"));
        OidcUser user = new DefaultOidcUser(authorities, idToken);

        return new OAuth2AuthenticationToken(user, authorities, "oidc");

    }
}