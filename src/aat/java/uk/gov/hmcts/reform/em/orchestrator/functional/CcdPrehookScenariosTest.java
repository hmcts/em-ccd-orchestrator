package uk.gov.hmcts.reform.em.orchestrator.functional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.em.orchestrator.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class CcdPrehookScenariosTest extends BaseTest {

    private final File jsonFile = new File(ClassLoader.getSystemResource("prehook-case.json").getPath());

    @Autowired
    protected CcdPrehookScenariosTest(
            TestUtil testUtil,
            ExtendedCcdHelper extendedCcdHelper
    ) {
        super(testUtil, extendedCcdHelper);
    }

    @BeforeEach
    public void setUp() {
        assumeFalse(enableCdamValidation);
    }

    @Test
    void testPostBundleStitch() {
        testUtil
                .authRequest()
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
    void testEndToEnd() throws IOException {
        final HashMap<String, String> caseData =
                testUtil
                        .authRequest()
                        .baseUri(testUtil.getTestUrl())
                        .contentType(APPLICATION_JSON_VALUE)
                        .body(jsonFile)
                        .post("/api/new-bundle")
                        .getBody()
                        .jsonPath()
                        .get("data");

        // pretend the user has modified some fields
        final String uploadedDocUri = testUtil.uploadDocument();
        final String caseJson = mapper.writeValueAsString(caseData)
                .replace("New Bundle", "Bundle title")
                .replace("hasCoversheets\":\"Yes", "hasCoversheets\":\"No")
                .replace("http://dm-store:8080/documents/05647df3-094c-45a3-b667-2a6f1bf3d088", uploadedDocUri);

        final String request = String.format("{ \"case_details\":{ \"case_data\": %s } } }", caseJson);

        testUtil
                .authRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(request)
                .post("/api/stitch-ccd-bundles")
                .then()
                .assertThat()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("New bundle"))
                .body("data.caseBundles[0].value.hasCoversheets", equalTo("No"))
                .log().all();
    }
}
