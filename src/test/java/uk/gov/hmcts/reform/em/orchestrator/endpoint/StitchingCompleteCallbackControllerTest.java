package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.service.notification.NotificationService;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.StitchingCompleteCallbackService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.TaskState;

import java.io.IOException;
import java.util.UUID;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, TestSecurityConfiguration.class})
@AutoConfigureMockMvc
public class StitchingCompleteCallbackControllerTest extends BaseTest {

    @MockBean
    private StitchingCompleteCallbackService stitchingCompleteCallbackService;

    @MockBean
    private NotificationService notificationService;

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
                .doNothing()
                .when(notificationService)
                    .sendEmailNotification(
                            Mockito.anyString(),
                            Mockito.anyString(),
                            Mockito.anyString(),
                            Mockito.anyString(),
                            Mockito.anyString());

        restLogoutMockMvc
                .perform(post("/api/stitching-complete-callback/abc/def/" + UUID.randomUUID())
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "xxx")
                        .header("ServiceAuthorization", "xxx"))
                .andDo(print()).andExpect(status().isOk());

    }

    @Test
    public void stitchingCompleteCallbackWithException() throws Exception {

        Mockito.doThrow(new CallbackException(456, "error", "error message"))
                .when(stitchingCompleteCallbackService).handleCallback(Mockito.any());

        restLogoutMockMvc
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
