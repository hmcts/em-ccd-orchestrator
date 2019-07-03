package uk.gov.hmcts.reform.em.orchestrator.functional;

import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.orchestrator.testutil.Env;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

import java.io.File;

public class AutomatedBundlingScenarios {

    private final TestUtil testUtil = new TestUtil();
    private final File jsonFile = new File(ClassLoader.getSystemResource("automated-case.json").getPath());

    @Test
    public void testPostBundleStitch() {
        Response response = testUtil.authRequest()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(jsonFile)
            .request("POST", Env.getTestUrl() + "/api/new-bundle");

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("New bundle", response.getBody().jsonPath().getString("data.caseBundles[0].value.title"));
    }

}
