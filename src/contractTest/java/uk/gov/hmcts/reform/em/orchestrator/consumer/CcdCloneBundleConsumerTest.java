package uk.gov.hmcts.reform.em.orchestrator.consumer;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import io.restassured.http.ContentType;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import static uk.gov.hmcts.reform.em.orchestrator.consumer.ConsumerTestUtil.createCloneRequestDsl;
import static uk.gov.hmcts.reform.em.orchestrator.consumer.ConsumerTestUtil.createCloneResponseDsl;

class CcdCloneBundleConsumerTest extends BaseConsumerTest {

    private static final String CLONE_PROVIDER_NAME = "em_orchestrator_clone_bundle_provider";
    private static final String CLONE_API_PATH = "/api/clone-ccd-bundles";

    @Pact(provider = CLONE_PROVIDER_NAME, consumer = ORCHESTRATOR_CONSUMER)
    public V4Pact cloneCcdBundle200(PactDslWithProvider builder) {
        return builder
            .given("a request to clone a bundle is successful")
            .uponReceiving("A POST request to clone a bundle")
            .path(CLONE_API_PATH)
            .method(HttpMethod.POST.toString())
            .headers(getHeaders())
            .body(createCloneRequestDsl())
            .willRespondWith()
            .status(HttpStatus.OK.value())
            .body(createCloneResponseDsl())
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "cloneCcdBundle200", providerName = CLONE_PROVIDER_NAME)
    void testCloneCcdBundle200(MockServer mockServer) {
        SerenityRest
            .given()
            .headers(getHeaders())
            .contentType(ContentType.JSON)
            .body(createCloneRequestDsl().getBody().toString())
            .post(mockServer.getUrl() + CLONE_API_PATH)
            .then()
            .statusCode(HttpStatus.OK.value());
    }
}