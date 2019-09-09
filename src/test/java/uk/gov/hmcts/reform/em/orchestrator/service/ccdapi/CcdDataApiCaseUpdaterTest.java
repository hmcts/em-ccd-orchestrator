package uk.gov.hmcts.reform.em.orchestrator.service.ccdapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CcdDataApiCaseUpdaterTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private CcdDataApiCaseUpdater ccdDataApiCaseUpdater;

    @Test
    public void executeUpdate() throws Exception {
        ccdDataApiCaseUpdater = buildTestedService(200, "OK");
        JsonNode jsonNode = objectMapper.readTree("{}");
        ccdDataApiCaseUpdater.executeUpdate("1", "jwt", jsonNode);
        assertTrue(true, "No exceptions");
    }

    @Test
    public void executeUpdateWith400Response() throws Exception {
        ccdDataApiCaseUpdater = buildTestedService(400, "OK");
        JsonNode jsonNode = objectMapper.readTree("{}");
        assertThrows(CallbackException.class, () ->
                ccdDataApiCaseUpdater.executeUpdate("1", "jwt", jsonNode));
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

    private CcdDataApiCaseUpdater buildTestedService(int httpStatus, String responseBody) {
        return new CcdDataApiCaseUpdater(mockHttp(httpStatus, responseBody), () -> "auth", "http://fake.url.com",
                objectMapper);
    }
}
