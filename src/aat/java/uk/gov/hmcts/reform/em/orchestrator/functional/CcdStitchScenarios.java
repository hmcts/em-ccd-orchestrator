package uk.gov.hmcts.reform.em.orchestrator.functional;

import io.restassured.response.ValidatableResponse;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class CcdStitchScenarios extends BaseTest {

    @Before
    public void setUp() throws Exception {
        Assume.assumeFalse(enableCdamValidation);
    }


    @Test
    public void testPostBundleStitchAsync() throws IOException, InterruptedException {
        CcdBundleDTO bundle = testUtil.getTestBundle();
        String json = mapper.writeValueAsString(new CcdValue<>(bundle));
        String wrappedJson = String.format("{ \"case_details\":{ \"case_data\":{ \"caseBundles\":[ %s ] } } }", json);

        ValidatableResponse response = postAsyncStitchCCDBundle(wrappedJson);
        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        response = testUtil.poll(documentTaskId);
        response
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Bundle title"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    private ValidatableResponse postAsyncStitchCCDBundle(String wrappedJson) {
        return testUtil
                .authRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE)
                .body(wrappedJson)
                .post("/api/async-stitch-ccd-bundles")
                .then();
    }
}
