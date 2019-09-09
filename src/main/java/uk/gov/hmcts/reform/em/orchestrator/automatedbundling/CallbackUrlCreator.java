package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

/**
 *
 * Creates a callback URL which is passed to the Stitching API.
 */
@Component
public class CallbackUrlCreator {

    private final UriComponentsBuilder uriComponentsBuilder;
    private final String host;
    private final String scheme;
    private final int port;

    public CallbackUrlCreator(@Value("${callbackUrlCreator.host}") String host,
                              @Value("${callbackUrlCreator.scheme}") String scheme,
                              @Value("${callbackUrlCreator.port}") int port) {
        this.uriComponentsBuilder = UriComponentsBuilder.newInstance();
        this.host = host;
        this.scheme = scheme;
        this.port = port;
    }

    public String createCallbackUrl(String caseId, String triggerId, UUID ccdBundleId) {

        UriComponents uriComponents = uriComponentsBuilder
                .scheme(scheme)
                .host(host)
                .port(port)
                .path(String.format("/api/stitching-complete-callback/%s/%s/%s",
                        caseId, triggerId, ccdBundleId.toString()))
                .build();

        return uriComponents.toString();

    }


}
