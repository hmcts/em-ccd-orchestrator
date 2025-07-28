package uk.gov.hmcts.reform.em.orchestrator.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static uk.gov.hmcts.reform.em.orchestrator.consumer.ConsumerTestUtil.buildCcdBundleDsl;
import static uk.gov.hmcts.reform.em.orchestrator.consumer.ConsumerTestUtil.buildCcdCallbackRequest;
import static uk.gov.hmcts.reform.em.orchestrator.consumer.ConsumerTestUtil.buildCcdCallbackResponse;

class CcdStitchBundleCallbackConsumerTest extends BaseConsumerTest {

    private static final String STITCH_PROVIDER_NAME = "em_orchestrator_stitch_provider";
    private static final String STITCH_API_PATH = "/api/stitch-ccd-bundles";
    private static final String ASYNC_STITCH_API_PATH = "/api/async-stitch-ccd-bundles";


    @Pact(provider = STITCH_PROVIDER_NAME, consumer = ORCHESTRATOR_CONSUMER)
    public V4Pact stitchCcdBundles200(PactDslWithProvider builder) {
        return builder
            .given("a request to stitch a bundle is successful")
            .uponReceiving("A POST request to synchronously stitch a bundle")
            .path(STITCH_API_PATH)
            .method(HttpMethod.POST.toString())
            .headers(getHeaders())
            .body(createStitchRequestDsl())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(createSyncStitchResponseDsl())
            .toPact(V4Pact.class);
    }

    @Pact(provider = STITCH_PROVIDER_NAME, consumer = ORCHESTRATOR_CONSUMER)
    public V4Pact asyncStitchCcdBundles200(PactDslWithProvider builder) {
        return builder
            .given("a request to asynchronously stitch a bundle is successful")
            .uponReceiving("A POST request to asynchronously stitch a bundle")
            .path(ASYNC_STITCH_API_PATH)
            .method(HttpMethod.POST.toString())
            .headers(getHeaders())
            .body(createStitchRequestDsl())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(createAsyncStitchResponseDsl())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "stitchCcdBundles200", providerName = STITCH_PROVIDER_NAME)
    void testStitchCcdBundles200(MockServer mockServer) {
        String requestBody = newJsonBody(body -> buildCcdCallbackRequest(body, data ->
            data.eachLike("caseBundles", bundle ->
                bundle.object("value", value -> value.stringType("eligibleForStitching", "yes"))
            )
        )).build().getBody().toString();

        SerenityRest
            .given()
            .headers(getHeaders())
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post(mockServer.getUrl() + STITCH_API_PATH)
            .then()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    @PactTestFor(pactMethod = "asyncStitchCcdBundles200", providerName = STITCH_PROVIDER_NAME)
    void testAsyncStitchCcdBundles200(MockServer mockServer) {
        String requestBody = newJsonBody(body -> buildCcdCallbackRequest(body, data ->
            data.eachLike("caseBundles", bundle ->
                bundle.object("value", value -> value.stringType("eligibleForStitching", "yes"))
            )
        )).build().getBody().toString();

        SerenityRest
            .given()
            .headers(getHeaders())
            .contentType(ContentType.JSON)
            .body(requestBody)
            .post(mockServer.getUrl() + ASYNC_STITCH_API_PATH)
            .then()
            .statusCode(HttpStatus.OK.value());
    }

    private DslPart createStitchRequestDsl() {
        return newJsonBody(body -> buildCcdCallbackRequest(body, data ->
            data.eachLike("caseBundles", bundle ->
                bundle.object("value", value ->
                    value.stringType("eligibleForStitching", "yes")
                )
            )
        )).build();
    }

    private DslPart createSyncStitchResponseDsl() {
        return newJsonBody(body -> buildCcdCallbackResponse(body, data ->
            data.eachLike("caseBundles", bundle ->
                bundle.object("value", value -> {
                    buildCcdBundleDsl(value);
                    value.object("stitchedDocument", ConsumerTestUtil::buildCcdDocumentDsl);
                })
            )
        )).build();
    }

    private DslPart createAsyncStitchResponseDsl() {
        return newJsonBody(body -> {
            buildCcdCallbackResponse(body, data ->
                data.eachLike("caseBundles", bundle ->
                    bundle.object("value", value -> {
                        buildCcdBundleDsl(value);
                        value.nullValue("stitchedDocument");
                    })
                )
            );
            body.numberType("documentTaskId", 98765L);
        }).build();
    }
}