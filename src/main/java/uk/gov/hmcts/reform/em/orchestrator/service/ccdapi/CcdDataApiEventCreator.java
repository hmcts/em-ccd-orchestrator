package uk.gov.hmcts.reform.em.orchestrator.service.ccdapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;

import java.io.IOException;

@Service
public class CcdDataApiEventCreator {

    private final OkHttpClient http;
    private final AuthTokenGenerator authTokenGenerator;
    private final String ccdDataBaseUrl;
    private final String CCD_CREATE_TRIGGER_ENDPOINT = "/cases/%s/event-triggers/%s";
    private final CcdCallbackDtoCreator ccdCallbackDtoCreator;

    public CcdDataApiEventCreator(OkHttpClient http, AuthTokenGenerator authTokenGenerator,
                                  @Value("${ccd.data.api.url}") String ccdDataBaseUrl,
                                  CcdCallbackDtoCreator ccdCallbackDtoCreator) {
        this.http = http;
        this.authTokenGenerator = authTokenGenerator;
        this.ccdDataBaseUrl = ccdDataBaseUrl;
        this.ccdCallbackDtoCreator = ccdCallbackDtoCreator;
    }

    public CcdCallbackDto executeTrigger(String caseId, String triggerId, String jwt) throws IOException, CallbackException {
        final Request request = new Request.Builder()
                .addHeader("Authorization", jwt)
                .addHeader("ServiceAuthorization", authTokenGenerator.generate())
                .url(String.format(ccdDataBaseUrl + CCD_CREATE_TRIGGER_ENDPOINT,
                        caseId,
                        triggerId))
                .get()
                .build();

        final Response response = http.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new CallbackException(response.code(), response.body().string(), "Creation of event-trigger failed");
        }

        return ccdCallbackDtoCreator.createDto(
                "caseBundles",
                jwt,
                response.body().charStream());

    }

}
