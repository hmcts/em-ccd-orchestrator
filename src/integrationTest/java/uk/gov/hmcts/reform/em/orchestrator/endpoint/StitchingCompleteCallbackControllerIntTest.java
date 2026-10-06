package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.service.notification.NotificationService;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.StitchingCompleteCallbackService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.TaskState;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = {Application.class, TestSecurityConfiguration.class})
@AutoConfigureMockMvc
class StitchingCompleteCallbackControllerIntTest extends BaseTest {

    @MockitoBean
    private StitchingCompleteCallbackService stitchingCompleteCallbackService;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private JsonMapper jsonMapper;

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
        documentTaskDTO.setJwt("must-not-appear-in-request-json");

        // Boot Jackson 3 mapper — proves @JsonIgnore jwt is honoured on the HTTP path
        requestBody = jsonMapper.writeValueAsString(documentTaskDTO);
        org.assertj.core.api.Assertions.assertThat(requestBody).doesNotContain("must-not-appear-in-request-json");
        org.assertj.core.api.Assertions.assertThat(requestBody).doesNotContain("\"jwt\"");
    }

    @Test
    void stitchingCompleteCallback() throws Exception {

        doNothing()
            .when(notificationService)
            .sendEmailNotification(
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString());

        mockMvc
            .perform(post("/api/stitching-complete-callback/abc/def/" + UUID.randomUUID())
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "xxx"))
            .andDo(print()).andExpect(status().isOk());

    }

    @Test
    void stitchingCompleteCallbackWithException() throws Exception {

        doThrow(new CallbackException(456, "error", "error message"))
            .when(stitchingCompleteCallbackService).handleCallback(any());

        mockMvc
            .perform(post("/api/stitching-complete-callback/abc/def/" + UUID.randomUUID())
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "xxx"))
            .andDo(print())
            .andExpect(status().is(456))
            .andExpect(jsonPath("$.message", Matchers.is("error message")))
            .andExpect(jsonPath("$.httpResponseBody", Matchers.is("error")))
            .andExpect(jsonPath("$.jwt").doesNotExist())
            .andExpect(result -> org.assertj.core.api.Assertions
                .assertThat(result.getResponse().getContentAsString())
                .doesNotContain("\"jwt\"")
                .doesNotContain("must-not-appear-in-request-json"));

    }

}