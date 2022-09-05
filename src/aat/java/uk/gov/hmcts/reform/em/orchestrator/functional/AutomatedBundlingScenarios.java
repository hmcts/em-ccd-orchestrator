package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class AutomatedBundlingScenarios extends BaseTest {

    private static JsonNode validJson;
    private static JsonNode invalidJson;
    private static JsonNode filenameJson;
    private static JsonNode invalidConfigJson;
    private static JsonNode filenameWith51CharsJson;
    private static JsonNode customDocumentsJson;
    private static JsonNode nonCustomDocumentsJson;
    private static JsonNode multiBundleDocumentsJson;

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    private RequestSpecification request;
    private RequestSpecification unAuthenticatedRequest;

    @Before
    public void setup() throws Exception {
        validJson = extendedCcdHelper.loadCaseFromFile("automated-case.json");
        invalidJson = extendedCcdHelper.loadCaseFromFile("invalid-automated-case.json");
        filenameJson = extendedCcdHelper.loadCaseFromFile("filename-case.json");
        invalidConfigJson = extendedCcdHelper.loadCaseFromFile("automated-case-invalid-configuration.json");
        filenameWith51CharsJson = extendedCcdHelper.loadCaseFromFile("filename-with-51-chars.json");
        customDocumentsJson = extendedCcdHelper.loadCaseFromFile("custom-documents-case.json");
        nonCustomDocumentsJson = extendedCcdHelper.loadCaseFromFile("non-custom-documents-case.json");
        multiBundleDocumentsJson = extendedCcdHelper.loadCaseFromFile("multi-bundle-case.json");
        setupRequests();
    }

    @Test
    public void testCreateBundle() throws IOException, InterruptedException {
        final ValidatableResponse response = postNewBundle(validJson);
        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("New bundle"))
                .body("data.caseBundles[0].value.folders[0].value.name", equalTo("Folder 1"))
                .body("data.caseBundles[0].value.folders[0].value.folders[0].value.name", equalTo("Folder 1.a"))
                .body("data.caseBundles[0].value.folders[0].value.folders[1].value.name", equalTo("Folder 1.b"))
                .body("data.caseBundles[0].value.folders[1].value.name", equalTo("Folder 2"))
                .body("data.caseBundles[0].value.fileName", equalTo("stitched.pdf"));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("New bundle"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    private ValidatableResponse postNewBundle(Object requestBody) {
        return request
                .body(requestBody)
                .post("/api/new-bundle")
                .then();
    }

    private void setupRequests() {
        request = testUtil
                .authRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE);

        unAuthenticatedRequest = testUtil
                .unauthenticatedRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE);
    }

    @NotNull
    private String findDocumentUrl(String json) {
        String url = testUtil.uploadDocument();
        json = json.replaceAll(
                "\"document_url\":\"documentUrl\"",
                String.format("\"document_url\":\"%s\"", url)
        );
        json = json.replaceAll(
                "\"document_binary_url\":\"documentUrl",
                String.format("\"document_binary_url\":\"%s", url)
        );
        return json;
    }
}
