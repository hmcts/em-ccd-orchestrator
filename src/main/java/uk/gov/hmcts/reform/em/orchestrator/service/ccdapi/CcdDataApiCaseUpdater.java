package uk.gov.hmcts.reform.em.orchestrator.service.ccdapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;

import java.io.IOException;

@Service
public class CcdDataApiCaseUpdater {

    private final OkHttpClient http;
    private final AuthTokenGenerator authTokenGenerator;
    private final String ccdDataBaseUrl;
    private final String ccdUpdateCasePath = "/cases/%s/events";
    private final ObjectMapper objectMapper;

    public CcdDataApiCaseUpdater(OkHttpClient http,
                                 AuthTokenGenerator authTokenGenerator,
                                 @Value("${ccd.data.api.url}") String ccdDataBaseUrl,
                                 ObjectMapper objectMapper) {
        this.http = http;
        this.authTokenGenerator = authTokenGenerator;
        this.ccdDataBaseUrl = ccdDataBaseUrl;
        this.objectMapper = objectMapper;
    }

    public void executeUpdate(String caseId, String jwt, JsonNode caseData) throws IOException, CallbackException {
        final String json = objectMapper.writeValueAsString(caseData);
        final RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        final Request updateRequest = new Request.Builder()
                .addHeader("Authorization", jwt)
                .addHeader("ServiceAuthorization", authTokenGenerator.generate())
                .url(String.format(ccdDataBaseUrl + ccdUpdateCasePath, caseId))
                .method("POST", body)
                .build();

        final Response updateResponse = http.newCall(updateRequest).execute();

        if (!updateResponse.isSuccessful()) {
            throw new CallbackException(updateResponse.code(), updateResponse.body().string(), "Update of case data failed");
        }

    }

}
