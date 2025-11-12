package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.em.orchestrator.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class StitchingCompleteScenariosTest extends BaseTest {
    JsonNode jsonNode;

    @Autowired
    protected StitchingCompleteScenariosTest(
            TestUtil testUtil,
            ExtendedCcdHelper extendedCcdHelper
    ) {
        super(testUtil, extendedCcdHelper);
    }

    @BeforeEach
    public void setup() throws IOException {
        assumeFalse(enableCdamValidation);
        jsonNode = extendedCcdHelper.loadCaseFromFile("automated-case.json");
    }

    @Test
    void testPostBundleStitchRequestMissing() throws IOException {
        String uploadedUrl = testUtil.uploadDocument();
        String documentString = extendedCcdHelper.getCcdDocumentJson("my doc text", uploadedUrl, "mydoc.txt");
        String caseId = extendedCcdHelper.createCase(documentString).getId().toString();
        ValidatableResponse response = postStitchingCompleteCallback(jsonNode, caseId,
            "68dd9a7d-90e5-4daf-8e50-643c68cf953b");

        response
                .assertThat().log().all()
                .statusCode(400)
                .body("message", equalTo("Bundle collection could not be found"))
                .body("localizedMessage", equalTo("Bundle collection could not be found"));
    }

    private ValidatableResponse postStitchingCompleteCallback(JsonNode wrappedJson, String caseId, String bundleId) {
        return testUtil
                .authRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .log().everything(true)
                .post("/api/stitching-complete-callback/{caseId}/{triggerId}/{bundleId}",
                        caseId,
                        "asyncStitchingComplete",
                        bundleId
                    )
                .then();
    }
}

