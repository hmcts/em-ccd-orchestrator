package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleCloningService;

import javax.servlet.http.HttpServletRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, TestSecurityConfiguration.class})
public class CcdCloneBundleController extends Base {

    @Test
    public void shouldCallCcdCallbackHandlerService() throws Exception {

        restLogoutMockMvc
                .perform(post("/api/clone-ccd-bundles")
                        .content("[]")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "xxx")
                        .header("ServiceAuthorization", "xxx"))
                .andDo(print()).andExpect(status().isOk());

        Mockito
                .verify(defaultUpdateCaller, Mockito.times(1))
                .executeUpdate(Mockito.any(CcdBundleCloningService.class), Mockito.any(HttpServletRequest.class));
    }
}
