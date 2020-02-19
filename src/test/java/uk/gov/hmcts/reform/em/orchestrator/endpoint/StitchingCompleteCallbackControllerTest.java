package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.service.ServiceRequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.core.user.UserRequestAuthorizer;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.service.notification.NotificationService;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.StitchingCompleteCallbackService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.TaskState;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
@AutoConfigureMockMvc
public class StitchingCompleteCallbackControllerTest {

    @MockBean
    private StitchingCompleteCallbackService stitchingCompleteCallbackService;

    @MockBean
    private ServiceRequestAuthorizer serviceRequestAuthorizer;

    @MockBean
    private UserRequestAuthorizer userRequestAuthorizer;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private MockMvc mockMvc;

    private String requestBody;

    @Before
    public void setUp() throws IOException {
        DocumentTaskDTO documentTaskDTO = new DocumentTaskDTO();
        StitchingBundleDTO stitchingBundleDTO = new StitchingBundleDTO();
        stitchingBundleDTO.setEnableEmailNotification(true);
        documentTaskDTO.setTaskState(TaskState.DONE);
        documentTaskDTO.setBundle(stitchingBundleDTO);

        ObjectMapper objectMapper = new ObjectMapper();
        requestBody = objectMapper.writeValueAsString(documentTaskDTO);
    }

    @Test
    public void stitchingCompleteCallback() throws Exception {
        Mockito
                .when(serviceRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new Service("ccd"));

        Mockito
                .when(userRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new User("john", Stream.of("caseworker").collect(Collectors.toSet())));

        Mockito
                .doNothing()
                .when(notificationService)
                    .sendEmailNotification(
                            Mockito.anyString(),
                            Mockito.anyString(),
                            Mockito.anyString(),
                            Mockito.anyString(),
                            Mockito.anyString());

        this.mockMvc
                .perform(post("/api/stitching-complete-callback/abc/def/" + UUID.randomUUID())
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "xxx")
                        .header("ServiceAuthorization", "xxx"))
                .andDo(print()).andExpect(status().isOk());

    }

    @Test
    public void stitchingCompleteCallbackWithException() throws Exception {
        Mockito
                .when(serviceRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new Service("ccd"));

        Mockito
                .when(userRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new User("john", Stream.of("caseworker").collect(Collectors.toSet())));


        Mockito.doThrow(new CallbackException(456, "error", "error message"))
                .when(stitchingCompleteCallbackService).handleCallback(Mockito.any());

        this.mockMvc
                .perform(post("/api/stitching-complete-callback/abc/def/" + UUID.randomUUID())
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "xxx")
                        .header("ServiceAuthorization", "xxx"))
                .andDo(print())
                .andExpect(status().is(456))
                .andExpect(jsonPath("$.message", Matchers.is("error message")))
                .andExpect(jsonPath("$.httpResponseBody", Matchers.is("error")));

    }
}
