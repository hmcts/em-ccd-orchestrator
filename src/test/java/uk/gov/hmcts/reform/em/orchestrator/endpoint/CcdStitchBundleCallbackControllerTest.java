package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.AsyncCcdBundleStitchingService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleStitchingService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CcdStitchBundleCallbackControllerTest {

    @Mock
    private DefaultUpdateCaller defaultUpdateCaller;

    @Mock
    private CcdBundleStitchingService ccdBundleStitchingService;

    @Mock
    private AsyncCcdBundleStitchingService asyncCcdBundleStitchingService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CcdStitchBundleCallbackController ccdStitchBundleCallbackController;

    @Test
    void shouldCallSyncStitchingService() {
        CcdCallbackResponseDto responseDto = new CcdCallbackResponseDto();
        ResponseEntity<CcdCallbackResponseDto> expectedResponse = ResponseEntity.ok(responseDto);

        when(defaultUpdateCaller.executeUpdate(ccdBundleStitchingService, request))
                .thenReturn(expectedResponse);

        ResponseEntity<CcdCallbackResponseDto> actualResponse =
                ccdStitchBundleCallbackController.stitchCcdBundles(request);

        assertEquals(expectedResponse, actualResponse);

        verify(defaultUpdateCaller).executeUpdate(ccdBundleStitchingService, request);
    }

    @Test
    void shouldCallAsyncStitchingService() {
        CcdCallbackResponseDto responseDto = new CcdCallbackResponseDto();
        ResponseEntity<CcdCallbackResponseDto> expectedResponse = ResponseEntity.ok(responseDto);

        when(defaultUpdateCaller.executeUpdate(asyncCcdBundleStitchingService, request))
                .thenReturn(expectedResponse);

        ResponseEntity<CcdCallbackResponseDto> actualResponse =
                ccdStitchBundleCallbackController.asyncStitchCcdBundles(request);

        assertEquals(expectedResponse, actualResponse);

        verify(defaultUpdateCaller).executeUpdate(asyncCcdBundleStitchingService, request);
    }
}