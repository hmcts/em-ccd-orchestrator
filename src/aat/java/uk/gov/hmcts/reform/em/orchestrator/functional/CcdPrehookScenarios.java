package uk.gov.hmcts.reform.em.orchestrator.functional;

import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.orchestrator.testutil.Env;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

import java.io.File;
import java.io.IOException;

public class CcdPrehookScenarios {

    private final TestUtil testUtil = new TestUtil();
    private final File jsonFile = new File(ClassLoader.getSystemResource("prehook-case.json").getPath());

    @Test
    public void testPostBundleStitch() throws IOException {
        Response response = testUtil.authRequest()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(jsonFile)
            .request("POST", Env.getTestUrl() + "/api/new-bundle");

        Assert.assertEquals(200, response.getStatusCode());
        // String body = response.getBody().prettyPrint();
        Assert.assertNotNull(response.getBody().jsonPath().getString("$case_data.case_details.caseBundles[0]"));
    }
}
