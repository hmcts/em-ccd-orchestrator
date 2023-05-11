package uk.gov.hmcts.reform.em.orchestrator.smoke;

import net.serenitybdd.rest.SerenityRest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestPropertySource(value = "classpath:application.yml")
public class SmokeTest {

    private static final String MESSAGE = "{\"message\":\"Welcome to EM Ccd Orchestrator API!\"}";

    @Value("${test.url}")
    private String testUrl;

    @Test
    public void testHealthEndpoint() {

        SerenityRest.useRelaxedHTTPSValidation();

        String response =
                SerenityRest
                        .given()
                        .baseUri(testUrl)
                        .get("/")
                        .then()
                        .statusCode(200).extract().body().asString();

        Assert.assertEquals(MESSAGE, response);


    }
}
