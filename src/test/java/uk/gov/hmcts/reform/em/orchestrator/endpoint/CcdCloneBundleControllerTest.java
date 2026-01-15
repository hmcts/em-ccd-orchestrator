package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleCloningService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CcdCloneBundleControllerTest {

    @Mock
    private DefaultUpdateCaller defaultUpdateCaller;

    @Mock
    private CcdBundleCloningService ccdBundleCloningService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CcdCloneBundleController ccdCloneBundleController;

    @Test
    void shouldCallDefaultUpdateCallerWithCloningService() {
        CcdCallbackResponseDto responseDto = new CcdCallbackResponseDto();
        ResponseEntity<CcdCallbackResponseDto> expectedResponse = ResponseEntity.ok(responseDto);

        when(defaultUpdateCaller.executeUpdate(ccdBundleCloningService, request))
                .thenReturn(expectedResponse);

        ResponseEntity<CcdCallbackResponseDto> actualResponse = ccdCloneBundleController.cloneCcdBundles(request);

        assertEquals(expectedResponse, actualResponse);
        
        verify(defaultUpdateCaller).executeUpdate(ccdBundleCloningService, request);
    }
}