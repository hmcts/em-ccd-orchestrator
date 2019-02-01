package uk.gov.hmcts.reform.em.orchestrator.smoke;

import io.restassured.RestAssured;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.testutil.Env;

public class SmokeTest {

    @Test
    public void testHealthEndpoint() {

        RestAssured.useRelaxedHTTPSValidation();

        RestAssured.given()
            .request("GET", Env.getTestUrl() + "/health")
            .then()
            .statusCode(200);


    }
}
