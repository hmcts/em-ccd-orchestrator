package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewBundleControllerTest {

    @Mock
    private DefaultUpdateCaller defaultUpdateCaller;

    @Mock
    private AutomatedCaseUpdater automatedCaseUpdater;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private NewBundleController newBundleController;

    @Test
    void shouldCallAutomatedCaseUpdater() {
        CcdCallbackResponseDto responseDto = new CcdCallbackResponseDto();
        ResponseEntity<CcdCallbackResponseDto> expectedResponse = ResponseEntity.ok(responseDto);

        when(defaultUpdateCaller.executeUpdate(automatedCaseUpdater, request))
                .thenReturn(expectedResponse);

        ResponseEntity<CcdCallbackResponseDto> actualResponse = newBundleController.prepareNewBundle(request);

        assertEquals(expectedResponse, actualResponse);

        verify(defaultUpdateCaller).executeUpdate(automatedCaseUpdater, request);
    }
}