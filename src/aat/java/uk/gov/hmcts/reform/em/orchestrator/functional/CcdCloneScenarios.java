package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import uk.gov.hmcts.reform.em.orchestrator.testutil.Env;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

import java.io.IOException;

public class CcdCloneScenarios {

    private final TestUtil testUtil = new TestUtil();
    private final ObjectMapper mapper = new ObjectMapper();

    private final Logger log = LoggerFactory.getLogger(CcdCloneScenarios.class);

    @Test
    public void testSingleBundleClone() throws IOException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        bundle.setEligibleForCloningAsBoolean(true);
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .request("POST", Env.getTestUrl() + "/api/clone-ccd-bundles");

        JsonPath path = response.getBody().jsonPath();
        String balooba = path.prettyPrint();
        if (!balooba.isEmpty()) {
            log.info(balooba);
        }

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("Bundle title", path.getString("data.caseBundles[0].value.title"));
        Assert.assertEquals("no", path.getString("data.caseBundles[0].value.eligibleForCloning"));
        Assert.assertEquals("Bundle title - CLONED", path.getString("data.caseBundles[1].value.title"));
        Assert.assertEquals("no", path.getString("data.caseBundles[1].value.eligibleForCloning"));
    }

    @Test
    public void testMultipleBundlesClone() throws IOException {
        CcdBundleDTO bundle1 = testUtil.getTestBundle();
        bundle1.setTitle("Bundle 1");

        CcdBundleDTO bundle2 = testUtil.getTestBundle();
        bundle2.setTitle("Bundle 2");
        bundle2.setEligibleForCloningAsBoolean(true);

        String jsonBundle1 = mapper.writeValueAsString(new CcdValue<>(bundle1));
        String jsonBundle2 = mapper.writeValueAsString(new CcdValue<>(bundle1));
        String jsonBundles = "{ " + jsonBundle1 + " }, { " + jsonBundle2 + " }";
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);
        log.info("wrapped Json is " + wrappedJson);

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .request("POST", Env.getTestUrl() + "/api/clone-ccd-bundles");

        JsonPath path = response.getBody().jsonPath();
        String balooba = path.prettyPrint();
        if (!balooba.isEmpty()) {
            log.info(balooba);
        }

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("Bundle 1", path.getString("data.caseBundles[0].value.title"));
        Assert.assertEquals("Bundle 2", path.getString("data.caseBundles[1].value.title"));
        Assert.assertEquals("Bundle 2 - CLONED", path.getString("data.caseBundles[2].value.title"));
        Assert.assertEquals("no", path.getString("data.caseBundles[1].value.eligibleForCloning"));

        Assert.assertEquals(2, StringUtils.countMatches(path.toString(), "CLONED"));
    }

}
