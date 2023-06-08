package uk.gov.hmcts.reform.em.orchestrator.functional;

import io.restassured.response.ValidatableResponse;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.testutil.Pair;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class SecureStitchingCompleteScenarios extends BaseTest {
    String wrappedJson;

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Before
    public void setup() throws Exception {
        Assume.assumeTrue(enableCdamValidation);
        wrappedJson = testUtil.addCdamProperties(extendedCcdHelper.loadCaseFromFile("automated-case.json"));
    }

    @Test
    public void testPostBundleStitchRequestMissing() throws Exception {
        List<Pair<String, String>> fileDetails = new ArrayList<>();
        fileDetails.add(Pair.of("annotationTemplate.pdf", "application/pdf"));
        String documentString = testUtil.uploadCdamDocuments(fileDetails);

        String caseId = extendedCcdHelper.createCdamCase(documentString).getId().toString();
        ValidatableResponse response = postStitchingCompleteCallback(wrappedJson, caseId, "68dd9a7d-90e5-4daf-8e50-643c68cf953b");

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

