package uk.gov.hmcts.reform.em.orchestrator.service.ccdapi;

import okhttp3.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class CcdDataApiEventCreatorTest {

    @Mock
    private CcdCallbackDtoCreator ccdCallbackDtoCreator;

    private CcdDataApiEventCreator ccdDataApiEventCreator;

    @Test
    public void executeTrigger() throws Exception {
        ccdDataApiEventCreator = buildTestedService(200, "OK");
        Mockito.when(ccdCallbackDtoCreator.createDto(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                Mockito.any())).thenReturn(new CcdCallbackDto());
        ccdDataApiEventCreator.executeTrigger("1", "triggerId", "jwt");
        assertTrue(true, "No exceptions");
    }

    @Test
    public void executeTriggerWith400Response() throws Exception {
        ccdDataApiEventCreator = buildTestedService(400, "OK");
        assertThrows(CallbackException.class, () ->
                ccdDataApiEventCreator.executeTrigger("1", "triggerId", "jwt"));
    }

    private OkHttpClient mockHttp(int httpStatus, String responseBody) {
        return new OkHttpClient
                .Builder()
                .addInterceptor(chain -> new Response.Builder()
                        .body(
                                ResponseBody.create(
                                        MediaType.get("application/json"),
                                        responseBody
                                )
                        )
                        .request(chain.request())
                        .message("")
                        .code(httpStatus)
                        .protocol(Protocol.HTTP_2)
                        .build())
                .build();
    }

    private CcdDataApiEventCreator buildTestedService(int httpStatus, String responseBody) {
        return new CcdDataApiEventCreator(
            mockHttp(httpStatus, responseBody),
            () -> "auth",
            "http://fake.url.com",
            ccdCallbackDtoCreator
        );

    }
}
