package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.*;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.orchestrator.testutil.Env;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static uk.gov.hmcts.reform.em.orchestrator.functional.TestSuiteInit.testUtil;

@Ignore
public class CcdPrehookScenarios {

    private final ObjectMapper mapper = new ObjectMapper();
    private final File jsonFile = new File(ClassLoader.getSystemResource("prehook-case.json").getPath());

    @BeforeClass
    public static void setup() throws Exception {
        testUtil.getCcdHelper().importCcdDefinitionFile();
    }

    @Test
    public void testPostBundleStitch() {
        Response response = testUtil.authRequest()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(jsonFile)
            .request("POST", Env.getTestUrl() + "/api/new-bundle");

        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("New Bundle", response.getBody().jsonPath().getString("data.caseBundles[0].value.title"));
    }

    @Test
    public void testEndToEnd() throws IOException {
        HashMap caseData = testUtil.authRequest()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(jsonFile)
            .request("POST", Env.getTestUrl() + "/api/new-bundle")
            .getBody()
            .jsonPath()
            .get("data");

        // pretend the user has modified some fields
        String uploadedDocUri = testUtil.uploadDocument();
        String caseJson = mapper.writeValueAsString(caseData)
            .replace("New Bundle", "Bundle title")
            .replace("hasCoversheets\":\"Yes", "hasCoversheets\":\"No")
            .replace("http://dm-store:8080/documents/05647df3-094c-45a3-b667-2a6f1bf3d088", uploadedDocUri);

        String request = String.format("{ \"case_details\":{ \"case_data\": %s } } }", caseJson);

        Response response = testUtil.authRequest()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(request)
            .request("POST", Env.getTestUrl() + "/api/stitch-ccd-bundles");

        JsonPath path = response.getBody().jsonPath();
        Assert.assertEquals(200, response.getStatusCode());
        Assert.assertEquals("Bundle title", path.getString("data.caseBundles[0].value.title"));
        Assert.assertEquals("No", path.getString("data.caseBundles[0].value.hasCoversheets"));
        Assert.assertNotNull(path.getString("data.caseBundles[0].value.stitchedDocument.document_url"));

    }
}
