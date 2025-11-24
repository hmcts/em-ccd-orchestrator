package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.em.orchestrator.Application;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {Application.class})
@AutoConfigureMockMvc
class NewBundleControllerTest extends BaseTest {

    @MockitoBean
    private DefaultUpdateCaller defaultUpdateCaller;

    @Test
    void shouldCallCcdCallbackHandlerService() throws Exception {
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