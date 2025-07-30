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

import java.util.UUID;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static uk.gov.hmcts.reform.em.orchestrator.consumer.ConsumerTestUtil.buildCcdCallbackRequest;
import static uk.gov.hmcts.reform.em.orchestrator.consumer.ConsumerTestUtil.buildCcdCallbackResponse;

class NewBundleConsumerTest extends BaseConsumerTest {

    private static final String NEW_BUNDLE_PROVIDER_NAME = "em_orchestrator_new_bundle_provider";
    private static final String NEW_BUNDLE_API_PATH = "/api/new-bundle";
    private static final String BUNDLE_ID = "a585a03b-a521-443b-826c-9411ebd44733";

    @Pact(provider = NEW_BUNDLE_PROVIDER_NAME, consumer = ORCHESTRATOR_CONSUMER)
    public V4Pact prepareNewBundle200(PactDslWithProvider builder) {
        return builder
            .given("a request to prepare a new bundle is successful")
            .uponReceiving("A POST request to prepare a new bundle")
            .path(NEW_BUNDLE_API_PATH)
            .method(HttpMethod.POST.toString())
            .headers(getHeaders())
            .body(createNewBundleRequestDsl())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(createPrepareNewBundleResponseDsl())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "prepareNewBundle200", providerName = NEW_BUNDLE_PROVIDER_NAME)
    void testPrepareNewBundle200(MockServer mockServer) {
        SerenityRest
            .given()
            .headers(getHeaders())
            .contentType(ContentType.JSON)
            .body(createNewBundleRequestDsl().getBody().toString())
            .post(mockServer.getUrl() + NEW_BUNDLE_API_PATH)
            .then()
            .statusCode(HttpStatus.OK.value());
    }

    private DslPart createNewBundleRequestDsl() {
        return newJsonBody(body -> buildCcdCallbackRequest(body, data -> {
            data.stringType("caseTitle", "My Test Case");
            data.eachLike("caseBundles", bundle ->
                bundle.object("value", value -> {
                    value.uuid("id", UUID.fromString(BUNDLE_ID));
                    value.stringType("title", "Test Bundle");
                })
            );
        })).build();
    }

    private DslPart createPrepareNewBundleResponseDsl() {
        return newJsonBody(body -> {
            buildCcdCallbackResponse(body, data -> {
                data.stringType("caseTitle", "My Test Case");
                data.eachLike("caseBundles", bundle ->
                    bundle.object("value", ConsumerTestUtil::buildCcdBundleDsl)
                );
            });
            body.numberType("documentTaskId", 12345L);
        }).build();
    }
}