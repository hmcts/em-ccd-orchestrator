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
    private final File validJson = new File(ClassLoader.getSystemResource("automated-case.json").getPath());
    private final File invalidJson = new File(ClassLoader.getSystemResource("invalid-automated-case.json").getPath());
    private final File filenameJson = new File(ClassLoader.getSystemResource("filename-case.json").getPath());

    @Test
    public void testCreateBundle() {
        Response response = testUtil.authRequest()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(validJson)
            .request("POST", Env.getTestUrl() + "/api/new-bundle");

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("New bundle", response.getBody().jsonPath().getString("data.caseBundles[0].value.title"));
        Assert.assertEquals("Folder 1", response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.name"));
        Assert.assertEquals("Folder 1.a", response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.folders[0].value.name"));
        Assert.assertEquals("Folder 1.b", response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.folders[1].value.name"));
        Assert.assertEquals("Folder 2", response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[1].value.name"));
        Assert.assertEquals("stitched.pdf", response.getBody().jsonPath().getString("data.caseBundles[0].value.fileName"));
    }

    @Test
    public void testInvalidConfig() {
        Response response = testUtil.authRequest()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(invalidJson)
            .request("POST", Env.getTestUrl() + "/api/new-bundle");

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("Unable to load configuration: does-not-exist.yaml", response.getBody().jsonPath().getString("errors[0]"));
    }

    @Test
    public void testFilename() {
        Response response = testUtil.authRequest()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(filenameJson)
            .request("POST", Env.getTestUrl() + "/api/new-bundle");

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("Bundle with filename", response.getBody().jsonPath().getString("data.caseBundles[0].value.title"));
        Assert.assertEquals("bundle.pdf", response.getBody().jsonPath().getString("data.caseBundles[0].value.fileName"));
    }

    @Test
    public void testTableOfContentsAndCoversheet() {
        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(validJson)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("Yes", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasCoversheets"));
        Assert.assertEquals("Yes", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasTableOfContents"));
        Assert.assertEquals("No", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasFolderCoversheets"));
    }

    @Test
    public void testFolderCoversheets() {
        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(filenameJson)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("No", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasCoversheets"));
        Assert.assertEquals("No", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasTableOfContents"));
        Assert.assertEquals("Yes", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasFolderCoversheets"));
    }

    @Test
    public void testSubSubfolders() {
        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(validJson)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        response.prettyPeek();
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertNull(response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.folders[0].value.folders"));
        Assert.assertNotNull(response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.folders[0].value.documents"));
    }
}
