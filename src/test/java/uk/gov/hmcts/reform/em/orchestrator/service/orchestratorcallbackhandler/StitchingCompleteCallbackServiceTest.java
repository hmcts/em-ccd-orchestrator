package uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdapi.CcdUpdateService;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class StitchingCompleteCallbackServiceTest {

    @Mock
    private CcdUpdateService ccdUpdateService;
    @Mock
    private CcdCallbackBundleUpdater ccdCallbackBundleUpdater;

    @InjectMocks
    private StitchingCompleteCallbackService stitchingCompleteCallbackService;

    @Test
    public void handleCallback() {
        Mockito
            .when(ccdUpdateService.startCcdEvent("a","1", "x"))
            .thenReturn(new CcdCallbackDto());
        stitchingCompleteCallbackService.handleCallback(new StitchingCompleteCallbackDto("x", "a",
                "1", UUID.randomUUID().toString(), new DocumentTaskDTO()));
        assertTrue(true, "No exception should be thrown");
    }

    @Test
    public void handleCallbackException()  {
        Mockito
                .when(ccdUpdateService.startCcdEvent("a","1", "x"))
                .thenThrow(new CallbackException(111, "err body", "err"));
        assertThrows(CallbackException.class, () ->
                stitchingCompleteCallbackService.handleCallback(new StitchingCompleteCallbackDto("x",
                        "a", "1", UUID.randomUUID().toString(), new DocumentTaskDTO())));
    }

    @Test
    public void handleCallbackExceptionFinally() {
        Mockito
                .when(ccdUpdateService.startCcdEvent(any(), any(), any()))
                .thenReturn(new CcdCallbackDto());

        Mockito.doThrow(new CallbackException(1, "", "")).when(ccdUpdateService)
                .submitCcdEvent(anyString(), anyString(), any(CcdCallbackDto.class));

        assertThrows(CallbackException.class, () ->
                stitchingCompleteCallbackService.handleCallback(new StitchingCompleteCallbackDto("x",
                        "a", "1", UUID.randomUUID().toString(), new DocumentTaskDTO())));
    }

    @Test
    public void handleCallbackExceptionFinallyNullCcdCallbackDto() {
        Mockito
                .when(ccdUpdateService.startCcdEvent(any(), any(), any()))
                .thenReturn(null);

        stitchingCompleteCallbackService.handleCallback(new StitchingCompleteCallbackDto("x",
                "a", "1", UUID.randomUUID().toString(), new DocumentTaskDTO()));

        Mockito.verify(ccdUpdateService, Mockito.never())
            .submitCcdEvent(anyString(), anyString(), any(CcdCallbackDto.class));

        assertTrue(true, "No exception should be thrown");
    }

}
