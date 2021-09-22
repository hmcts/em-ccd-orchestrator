package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
@Ignore
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
    public void testCreateBundle() {
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
    }

    @Test
    public void testInvalidConfig() {
        final ValidatableResponse response = postNewBundle(invalidJson);
        response
                .assertThat().log().all()
                .statusCode(200) //FIXME should be 400
                .body("errors[0]", equalTo("Invalid configuration file entry in: does-not-exist.yaml"
                    + "; Configuration file parameter(s) and/or parameter value(s)"));

    }

    @Test
    public void testCorruptConfig() {
        final ValidatableResponse response = postNewBundle(invalidConfigJson);

        response
                .assertThat().log().all()
                .statusCode(200) //FIXME should be 400
                .body("errors[0]", equalTo("Invalid configuration file entry in: example-incorrect-key.yaml"
                    + "; Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    public void testFilename() {
        final ValidatableResponse response = postNewBundle(filenameJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle with filename"))
                .body("data.caseBundles[0].value.fileName", equalTo("bundle.pdf"));
    }

    @Test
    public void testTableOfContentsAndCoversheet() {
        final ValidatableResponse response = postNewBundle(validJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.hasCoversheets", equalTo("Yes"))
                .body("data.caseBundles[0].value.hasTableOfContents", equalTo("Yes"))
                .body("data.caseBundles[0].value.hasFolderCoversheets", equalTo("No"));
    }

    @Test
    public void testFolderCoversheets() {
        final ValidatableResponse response = postNewBundle(filenameJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.hasCoversheets", equalTo("No"))
                .body("data.caseBundles[0].value.hasTableOfContents", equalTo("No"))
                .body("data.caseBundles[0].value.hasFolderCoversheets", equalTo("Yes"));
    }

    @Test
    public void testSubSubfolders() {
        final ValidatableResponse response = postNewBundle(validJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders[0].value.folders[0].value.folders", nullValue())
                .body("data.caseBundles[0].value.folders[0].value.folders[0].value.documents", notNullValue());
    }

    @Test
    public void testAddFlatDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-1-flat-docs.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.documents", hasSize(4))
                .body("data.caseBundles[0].value.documents[0].value.name", equalTo("Prosecution doc 1"))
                .body("data.caseBundles[0].value.documents[1].value.name", equalTo("Prosecution doc 2"))
                .body("data.caseBundles[0].value.documents[2].value.name", equalTo("Evidence doc"))
                .body("data.caseBundles[0].value.documents[3].value.name", equalTo("Defendant doc 1"));
    }

    @Test
    public void testAddFlatFilteredDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-2-filter-flat-docs.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.documents", hasSize(2))
                .body("data.caseBundles[0].value.documents[0].value.name", equalTo("Prosecution doc 1"))
                .body("data.caseBundles[0].value.documents[1].value.name", equalTo("Prosecution doc 2"));
    }

    @Test
    public void testAddFolderedDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-3-foldered-docs.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders", hasSize(2))
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(4))
                .body("data.caseBundles[0].value.folders[0].value.documents[0].value.name", equalTo("Prosecution doc 1"))
                .body("data.caseBundles[0].value.folders[0].value.documents[1].value.name", equalTo("Prosecution doc 2"))
                .body("data.caseBundles[0].value.folders[0].value.documents[2].value.name", equalTo("Defendant doc 1"))
                .body("data.caseBundles[0].value.folders[0].value.documents[3].value.name", equalTo("Evidence doc"))
                .body("data.caseBundles[0].value.folders[1].value.documents", hasSize(1))
                .body("data.caseBundles[0].value.folders[1].value.documents[0].value.name", equalTo("Single doc 1"));
    }

    @Test
    public void testAddFilteredFolderedDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-4-filtered-foldered-docs.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders", hasSize(2))
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(2))
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(2))
                .body("data.caseBundles[0].value.folders[0].value.documents[0].value.name", equalTo("Prosecution doc 1"))
                .body("data.caseBundles[0].value.folders[0].value.documents[1].value.name", equalTo("Prosecution doc 2"))
                .body("data.caseBundles[0].value.folders[1].value.documents", hasSize(1))
                .body("data.caseBundles[0].value.folders[1].value.documents[0].value.name", equalTo("Single doc 1"));
    }

    @Test
    public void testTypoInConfigurationFile() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-6-has-typo.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200) //FIXME should be 400
                .body("errors", contains("Invalid configuration file entry in: f-tests-6-has-typo.yaml; "
                    + "Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    public void testDefaultFallBackConfigurationFile() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)  //FIXME should be 400
                .body("errors", contains("Invalid configuration file entry in: configurationFile; Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    public void testDocumentNotPresent() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-12-invalid-document-property.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(2));
    }

    @Test
    public void testDocumentPropertyIsAnArray() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-7-not-a-single-doc.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)   //FIXME should be 400
                .body("errors", contains("Element is an array: /caseDocuments"));
    }

    @Test
    public void testDocumentSetPropertyIsNotAnArray() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-8-not-an-array.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)  //FIXME should be 400
                .body("errors", contains("Element is not an array: /singleDocument"));
    }

    @Test
    public void testDocumentStructureCorrupted() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("document_url", "incorrect_property_name");
        json = json.replaceAll("configurationFile", "f-tests-5-invalid-url.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)  //FIXME should be 400
                .body("errors", contains("Could not find the property /documentLink/document_url in the node: "));
    }

    @Test
    public void testConfigurationFileDoesNotExist() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "nonexistent.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)  //FIXME should be 400
                .body("errors", contains("Invalid configuration file entry in: nonexistent.yaml; Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    public void testMultipleFilters() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-9-multiple-filters.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders", hasSize(1))
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(3))
                .body("data.caseBundles[0].value.folders[0].value.documents[0].value.name", equalTo("Prosecution doc 1"))
                .body("data.caseBundles[0].value.folders[0].value.documents[1].value.name", equalTo("Prosecution doc 2"))
                .body("data.caseBundles[0].value.folders[0].value.documents[2].value.name", equalTo("Evidence doc"));
    }

    @Test
    public void testSortDocumentsAscending() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-10-sorting.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders", hasSize(2))
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(4))
                .body("data.caseBundles[0].value.folders[0].value.documents[0].value.name", equalTo("Prosecution doc 1"))
                .body("data.caseBundles[0].value.folders[0].value.documents[1].value.name", equalTo("Prosecution doc 2"))
                .body("data.caseBundles[0].value.folders[0].value.documents[2].value.name", equalTo("Evidence doc"))
                .body("data.caseBundles[0].value.folders[0].value.documents[3].value.name", equalTo("Defendant doc 1"))
                .body("data.caseBundles[0].value.folders[1].value.documents", hasSize(1))
                .body("data.caseBundles[0].value.folders[1].value.documents[0].value.name", equalTo("Single doc 1"));
    }

    @Test
    public void testSortDocumentsDescending() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-11-sorting.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders", hasSize(2))
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(4))
                .body("data.caseBundles[0].value.folders[0].value.documents[0].value.name", equalTo("Defendant doc 1"))
                .body("data.caseBundles[0].value.folders[0].value.documents[1].value.name", equalTo("Evidence doc"))
                .body("data.caseBundles[0].value.folders[0].value.documents[2].value.name", equalTo("Prosecution doc 2"))
                .body("data.caseBundles[0].value.folders[0].value.documents[3].value.name", equalTo("Prosecution doc 1"))
                .body("data.caseBundles[0].value.folders[1].value.documents", hasSize(1))
                .body("data.caseBundles[0].value.folders[1].value.documents[0].value.name", equalTo("Single doc 1"));
    }

    @Test
    public void testEnableEmailNotificationIsNull() throws IOException {
        final ValidatableResponse response = postNewBundle(validJson);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.enableEmailNotification", nullValue());
    }

    @Test
    public void testRenderImageInStitchedDocument() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-13-render-image-flat-docs.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.documentImage.docmosisAssetId", equalTo("hmcts.png"))
                .body("data.caseBundles[0].value.documentImage.imageRenderingLocation", equalTo("allPages"))
                .body("data.caseBundles[0].value.documentImage.imageRendering", equalTo("opaque"))
                .body("data.caseBundles[0].value.documentImage.coordinateX", equalTo(50))
                .body("data.caseBundles[0].value.documentImage.coordinateY", equalTo(50));
    }

    @Test
    public void testRedactedDocuments() throws IOException {

        final ValidatableResponse response = postNewBundle(customDocumentsJson);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(4))
                .body("data.caseBundles[0].value.folders[0].value.documents[0].value.name", equalTo("Non Redacted Doc1.pdf"))
                .body("data.caseBundles[0].value.folders[0].value.documents[1].value.name", equalTo("Redacted Doc2.pdf"))
                .body("data.caseBundles[0].value.folders[0].value.documents[2].value.name", equalTo("Redacted Doc3.pdf"))
                .body("data.caseBundles[0].value.folders[0].value.documents[3].value.name", equalTo("AT38.png"));
    }

    @Test
    public void testNonRedactedDocuments() {

        final ValidatableResponse response = postNewBundle(nonCustomDocumentsJson);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(3))
                .body("data.caseBundles[0].value.folders[0].value.documents[0].value.name", equalTo("Non Redacted Doc1.pdf"))
                .body("data.caseBundles[0].value.folders[0].value.documents[1].value.name", equalTo("DWP response.pdf"))
                .body("data.caseBundles[0].value.folders[0].value.documents[2].value.name", equalTo("DWP evidence.pdf"));
    }

    @Test
    public void testMultiBundleDocuments() {

        final ValidatableResponse response = postNewBundle(multiBundleDocumentsJson);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles", hasSize(2));
    }

    @Test
    public void shouldReturn401WhenUnAuthenticatedUserCreateBundle() {
        unAuthenticatedRequest
                .body(validJson)
                .post("/api/new-bundle")
                .then()
                .log().all()
                .statusCode(401);
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
}
