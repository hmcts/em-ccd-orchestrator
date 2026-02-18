package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.AsyncCcdBundleStitchingService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleStitchingService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class})
@AutoConfigureMockMvc
@TestPropertySource(properties = {"notify.apiKey=test-api-key-dummy"})
class CcdStitchBundleCallbackControllerIntTest extends BaseTest {

    @MockitoBean
    private DefaultUpdateCaller defaultUpdateCaller;

    @Test
    void shouldCallCcdCallbackHandlerService() throws Exception {

        mockMvc
            .perform(post("/api/stitch-ccd-bundles")
                .content("[]")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "xxx"))
            .andDo(print()).andExpect(status().isOk());

        Mockito
            .verify(defaultUpdateCaller, Mockito.times(1))
            .executeUpdate(any(CcdBundleStitchingService.class), any(HttpServletRequest.class));
    }

    @Test
    void shouldCallCcdCallbackHandlerServiceAsync() throws Exception {

        mockMvc
            .perform(post("/api/async-stitch-ccd-bundles")
                .content("[]")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "xxx"))
            .andDo(print()).andExpect(status().isOk());

        Mockito
            .verify(defaultUpdateCaller, Mockito.times(1))
            .executeUpdate(any(AsyncCcdBundleStitchingService.class),
                any(HttpServletRequest.class));
    }

}