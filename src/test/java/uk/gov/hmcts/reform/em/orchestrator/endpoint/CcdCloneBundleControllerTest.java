package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleCloningService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
@TestPropertySource(
        properties = {
                "case_document_am.url=http://localhost:8090"
        }
)
public class CcdCloneBundleControllerTest extends BaseTest {

    @MockBean
    private DefaultUpdateCaller defaultUpdateCaller;

    @Test
    public void shouldCallCcdCallbackHandlerService() throws Exception {

        mockMvc
                .perform(post("/api/clone-ccd-bundles")
                        .content("[]")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "xxx"))
                .andDo(print()).andExpect(status().isOk());

        Mockito
                .verify(defaultUpdateCaller, Mockito.times(1))
                .executeUpdate(Mockito.any(CcdBundleCloningService.class), Mockito.any(HttpServletRequest.class));
    }
}
