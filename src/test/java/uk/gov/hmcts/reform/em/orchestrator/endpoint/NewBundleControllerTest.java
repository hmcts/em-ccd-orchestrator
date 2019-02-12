package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
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
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackHandlerService;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class NewBundleControllerTest {
    @MockBean
    private CcdCallbackDtoCreator ccdCallbackDtoCreator;

    @MockBean
    private CcdCallbackHandlerService ccdCallbackHandlerService;

    @MockBean
    private ServiceRequestAuthorizer serviceRequestAuthorizer;

    @MockBean
    private UserRequestAuthorizer userRequestAuthorizer;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldCallCcdCallbackHandlerService() throws Exception {

        Mockito
                .when(serviceRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new Service("ccd"));

        Mockito
                .when(userRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new User("john", Stream.of("caseworker").collect(Collectors.toSet())));

        Mockito
                .when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
                .thenReturn(new CcdCallbackDto());

        this.mockMvc
                .perform(post("/api/new-bundle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "xxx")
                        .header("ServiceAuthorization", "xxx"))
                .andDo(print()).andExpect(status().isOk());

        Mockito
                .verify(ccdCallbackHandlerService, Mockito.times(1))
                .handleCddCallback(Mockito.any(CcdCallbackDto.class));
    }

    @Test
    public void shouldCallCcdCallbackHandlerServiceUpdateException() throws Exception {

        Mockito
                .when(serviceRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new Service("ccd"));

        Mockito
                .when(userRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new User("john", Stream.of("caseworker").collect(Collectors.toSet())));

        Mockito
                .when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
                .thenReturn(new CcdCallbackDto());

        Mockito
                .when(ccdCallbackHandlerService.handleCddCallback(Mockito.any(CcdCallbackDto.class)))
                .thenThrow(new RuntimeException("test message"));

        this.mockMvc
                .perform(post("/api/new-bundle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "xxx")
                        .header("ServiceAuthorization", "xxx"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors[0]", Matchers.is("test message")));

        Mockito
                .verify(ccdCallbackHandlerService, Mockito.times(1))
                .handleCddCallback(Mockito.any(CcdCallbackDto.class));
    }


}