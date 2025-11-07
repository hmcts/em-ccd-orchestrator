package uk.gov.hmcts.reform.em.orchestrator.functional;

import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.em.orchestrator.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.em.orchestrator.testutil.Pair;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class SecureStitchingCompleteScenariosTest extends BaseTest {
    String wrappedJson;

    @Autowired
    protected SecureStitchingCompleteScenariosTest(
            TestUtil testUtil,
            ExtendedCcdHelper extendedCcdHelper
    ) {
        super(testUtil, extendedCcdHelper);
    }

    @BeforeEach
    public void setup() throws IOException {
        assumeTrue(enableCdamValidation);
        wrappedJson = testUtil.addCdamProperties(extendedCcdHelper.loadCaseFromFile("automated-case.json"));
    }

    @Test
    void testPostBundleStitchRequestMissing() throws IOException {
        List<Pair<String, String>> fileDetails = new ArrayList<>();
        fileDetails.add(Pair.of("annotationTemplate.pdf", "application/pdf"));
        String documentString = testUtil.uploadCdamDocuments(fileDetails);

        String caseId = extendedCcdHelper.createCdamCase(documentString).getId().toString();
        ValidatableResponse response = postStitchingCompleteCallback(
            wrappedJson, caseId, "68dd9a7d-90e5-4daf-8e50-643c68cf953b");

        response
                .assertThat().log().all()
                .statusCode(400)
                .body("message", equalTo("Bundle collection could not be found"))
                .body("localizedMessage", equalTo("Bundle collection could not be found"));
    }

    private ValidatableResponse postStitchingCompleteCallback(String wrappedJson, String caseId, String bundleId) {
        return testUtil
                .cdamAuthRequest()
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

