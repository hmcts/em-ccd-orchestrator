package uk.gov.hmcts.reform.em.orchestrator.endpoint;

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
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleStitchingService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.InputValidationException;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CcdStitchBundleCallbackControllerTest {

    @MockBean
    private CcdCallbackDtoCreator ccdCallbackDtoCreator;

    @MockBean
    private CcdBundleStitchingService ccdBundleStitchingService;

    @MockBean
    private ServiceRequestAuthorizer serviceRequestAuthorizer;

    @MockBean
    private UserRequestAuthorizer userRequestAuthorizer;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setupMocks() {
        Mockito
                .when(serviceRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new Service("ccd"));

        Mockito
                .when(userRequestAuthorizer.authorise(Mockito.any(HttpServletRequest.class)))
                .thenReturn(new User("john", Stream.of("caseworker").collect(Collectors.toSet())));

        Mockito
                .when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
                .thenReturn(new CcdCallbackDto());
    }

    @Test
    public void shouldCallCcdCallbackHandlerService() throws Exception {

        this.mockMvc
                .perform(post("/api/stitch-ccd-bundles")
                        .content("[]")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "xxx")
                        .header("ServiceAuthorization", "xxx"))
                .andDo(print()).andExpect(status().isOk());

        Mockito
                .verify(ccdBundleStitchingService, Mockito.times(1))
                .updateCase(Mockito.any(CcdCallbackDto.class));
    }

    @Test
    public void shouldCallCcdCallbackHandlerServiceUpdateException() throws Exception {

        Mockito
                .when(ccdBundleStitchingService.updateCase(Mockito.any(CcdCallbackDto.class)))
                .thenThrow(new RuntimeException("test message"));

        this.mockMvc
                .perform(post("/api/stitch-ccd-bundles")
                        .content("[]")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "xxx")
                        .header("ServiceAuthorization", "xxx"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors[0]", Matchers.is("test message")));

        Mockito
                .verify(ccdBundleStitchingService, Mockito.times(1))
                .updateCase(Mockito.any(CcdCallbackDto.class));
    }

    @Test
    public void shouldCallCcdCallbackHandlerServiceInputValidationException() throws Exception {
        Set<ConstraintViolation<CcdBundleDTO>> violations = new HashSet<>();

        Mockito
            .when(ccdBundleStitchingService.updateCase(Mockito.any(CcdCallbackDto.class)))
            .thenThrow(new InputValidationException(violations));

        this.mockMvc
            .perform(post("/api/stitch-ccd-bundles")
                .content("[]")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "xxx")
                .header("ServiceAuthorization", "xxx"))
            .andDo(print())
            .andExpect(status().isOk());

        Mockito
            .verify(ccdBundleStitchingService, Mockito.times(1))
            .updateCase(Mockito.any(CcdCallbackDto.class));
    }
}
