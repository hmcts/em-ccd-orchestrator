package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import javax.servlet.http.HttpServletRequest;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
@AutoConfigureMockMvc
public class NewBundleControllerTest extends BaseTest {

    @MockBean
    private DefaultUpdateCaller defaultUpdateCaller;

    @Test
    public void shouldCallCcdCallbackHandlerService() throws Exception {
        CcdCallbackResponseDto ccdCallbackResponseDto = new CcdCallbackResponseDto();
        ccdCallbackResponseDto.setErrors(Collections.emptyList());
        ResponseEntity<CcdCallbackResponseDto> response = ResponseEntity.ok(ccdCallbackResponseDto);
        Mockito.when(defaultUpdateCaller.executeUpdate(
                Mockito.any(AutomatedCaseUpdater.class), Mockito.any(HttpServletRequest.class)))
                .thenReturn(response);

        mockMvc
                .perform(post("/api/new-bundle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "xxx"))
                .andDo(print()).andExpect(status().isOk());

        Mockito
                .verify(defaultUpdateCaller, Mockito.times(1))
                .executeUpdate(Mockito.any(AutomatedCaseUpdater.class), Mockito.any(HttpServletRequest.class));
    }

}
