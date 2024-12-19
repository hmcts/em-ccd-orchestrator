package uk.gov.hmcts.reform.em.orchestrator.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.File;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class SecureCcdPrehookScenariosTest extends BaseTest {

    private final File jsonFile = new File(ClassLoader.getSystemResource("prehook-case-cdam.json").getPath());

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @BeforeEach
    public void setUp() throws Exception {
        assumeTrue(enableCdamValidation);
    }


    @Test
    void testPostBundleStitch() {
        testUtil
                .cdamAuthRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(jsonFile)
                .post("/api/new-bundle")
                .then()
                .assertThat()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("New bundle"))
                .log().all();
    }

    @Test
    void testEndToEnd() throws Exception {
        final HashMap<String, String> caseData =
                testUtil
                        .cdamAuthRequest()
                        .baseUri(testUtil.getTestUrl())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body(jsonFile)
                        .post("/api/new-bundle")
                        .getBody()
                        .jsonPath()
                        .get("data");

        // pretend the user has modified some fields
        final String uploadedDocUri = testUtil.uploadCdamDocument().self.href;
        final String caseJson = mapper.writeValueAsString(caseData)
                .replace("New Bundle", "Bundle title")
                .replace("hasCoversheets\":\"Yes", "hasCoversheets\":\"No")
                .replace("http://dm-store:8080/documents/05647df3-094c-45a3-b667-2a6f1bf3d088", uploadedDocUri);

        String request = String.format("{ \"case_details\":{ \"case_data\": %s } } }", caseJson);

        String body = testUtil.addCdamProperties(request);

        testUtil
                .cdamAuthRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(body)
                .post("/api/stitch-ccd-bundles")
                .then()
                .assertThat()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("New bundle"))
                .body("data.caseBundles[0].value.hasCoversheets", equalTo("No"))
                .log().all();
    }
}
