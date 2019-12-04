package uk.gov.hmcts.reform.em.orchestrator.smoke;

import io.restassured.RestAssured;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.em.EmTestConfig;

@SpringBootTest(classes = {EmTestConfig.class})
@PropertySource(value = "classpath:application.yml")
@RunWith(SpringRunner.class)
public class SmokeTest {

    @Value("${test.url}")
    private String testUrl;

    @Test
    public void testHealthEndpoint() {

        RestAssured.useRelaxedHTTPSValidation();

        RestAssured.given()
            .request("GET", testUrl + "/health")
            .then()
            .statusCode(200);


    }
}
