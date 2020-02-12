package uk.gov.hmcts.reform.em.orchestrator.endpoint;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;

import javax.servlet.http.HttpServletRequest;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
@AutoConfigureMockMvc
public class NewBundleControllerTest {
    @MockBean
    private DefaultUpdateCaller defaultUpdateCaller;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldCallCcdCallbackHandlerService() throws Exception {

        this.mockMvc
                .perform(post("/api/new-bundle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "xxx")
                        .header("ServiceAuthorization", "xxx"))
                .andDo(print()).andExpect(status().isOk());

        Mockito
                .verify(defaultUpdateCaller, Mockito.times(1))
                .executeUpdate(Mockito.any(AutomatedCaseUpdater.class), Mockito.any(HttpServletRequest.class));
    }

}
