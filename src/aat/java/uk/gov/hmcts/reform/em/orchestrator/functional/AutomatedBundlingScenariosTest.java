package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class AutomatedBundlingScenariosTest extends BaseTest {

    private static JsonNode validJson;
    private static JsonNode invalidJson;
    private static JsonNode filenameJson;
    private static JsonNode invalidConfigJson;
    private static JsonNode customDocumentsJson;
    private static JsonNode nonCustomDocumentsJson;
    private static JsonNode multiBundleDocumentsJson;

    private RequestSpecification request;
    private RequestSpecification unAuthenticatedRequest;

    @BeforeEach
    public void setup() throws Exception {
        assumeFalse(enableCdamValidation);
        validJson = extendedCcdHelper.loadCaseFromFile("automated-case.json");
        invalidJson = extendedCcdHelper.loadCaseFromFile("invalid-automated-case.json");
        filenameJson = extendedCcdHelper.loadCaseFromFile("filename-case.json");
        invalidConfigJson = extendedCcdHelper.loadCaseFromFile("automated-case-invalid-configuration.json");
        customDocumentsJson = extendedCcdHelper.loadCaseFromFile("custom-documents-case.json");
        nonCustomDocumentsJson = extendedCcdHelper.loadCaseFromFile("non-custom-documents-case.json");
        multiBundleDocumentsJson = extendedCcdHelper.loadCaseFromFile("multi-bundle-case.json");
        setupRequests();
    }

    @Test
    void testCreateBundle() {
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

    @Test
    void testInvalidConfig() {
        final ValidatableResponse response = postNewBundle(invalidJson);
        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", equalTo("Invalid configuration file entry in: does-not-exist.yaml"
                    + "; Configuration file parameter(s) and/or parameter value(s)"));

    }

    @Test
    void testCorruptConfig() {
        final ValidatableResponse response = postNewBundle(invalidConfigJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", equalTo("Invalid configuration file entry in: "
                        + "testbundleconfiguration/example-incorrect-key.yaml"
                        + "; Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    void testFilename() {
        final ValidatableResponse response = postNewBundle(filenameJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("Bundle with filename"))
                .body("data.caseBundles[0].value.fileName", equalTo("bundle.pdf"));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Bundle with filename"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testTableOfContentsAndCoversheet() {
        final ValidatableResponse response = postNewBundle(validJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.hasCoversheets", equalTo("Yes"))
                .body("data.caseBundles[0].value.hasTableOfContents", equalTo("Yes"))
                .body("data.caseBundles[0].value.hasFolderCoversheets", equalTo("No"));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("New bundle"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testFolderCoversheets() {
        final ValidatableResponse response = postNewBundle(filenameJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.hasCoversheets", equalTo("No"))
                .body("data.caseBundles[0].value.hasTableOfContents", equalTo("No"))
                .body("data.caseBundles[0].value.hasFolderCoversheets", equalTo("Yes"));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Bundle with filename"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testSubSubfolders() {
        final ValidatableResponse response = postNewBundle(validJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders[0].value.folders[0].value.folders", nullValue())
                .body("data.caseBundles[0].value.folders[0].value.folders[0].value.documents", notNullValue());

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("New bundle"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testAddFlatDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "testbundleconfiguration/f-tests-1-flat-docs.yaml");
        json = findDocumentUrl(json);

        final ValidatableResponse response = postNewBundle(json);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.documents", hasSize(4))
                .body("data.caseBundles[0].value.documents[0].value.name", equalTo("Prosecution doc 1"))
                .body("data.caseBundles[0].value.documents[1].value.name", equalTo("Prosecution doc 2"))
                .body("data.caseBundles[0].value.documents[2].value.name", equalTo("Evidence doc"))
                .body("data.caseBundles[0].value.documents[3].value.name", equalTo("Defendant doc 1"));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Functional tests bundle 1"))
                .body("bundle.stitchedDocumentURI", notNullValue());

    }

    @Test
    void testAddFlatFilteredDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "testbundleconfiguration/f-tests-2-filter-flat-docs.yaml");
        json = findDocumentUrl(json);

        final ValidatableResponse response = postNewBundle(json);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.documents", hasSize(2))
                .body("data.caseBundles[0].value.documents[0].value.name", equalTo("Prosecution doc 1"))
                .body("data.caseBundles[0].value.documents[1].value.name", equalTo("Prosecution doc 2"));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Functional tests bundle 2"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testAddFolderedDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "testbundleconfiguration/f-tests-3-foldered-docs.yaml");
        json = findDocumentUrl(json);

        final ValidatableResponse response = postNewBundle(json);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders", hasSize(2))
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(4))
                .body("data.caseBundles[0].value.folders[0].value.documents[0].value.name",
                    equalTo("Prosecution doc 1"))
                .body("data.caseBundles[0].value.folders[0].value.documents[1].value.name",
                    equalTo("Prosecution doc 2"))
                .body("data.caseBundles[0].value.folders[0].value.documents[2].value.name",
                    equalTo("Defendant doc 1"))
                .body("data.caseBundles[0].value.folders[0].value.documents[3].value.name",
                    equalTo("Evidence doc"))
                .body("data.caseBundles[0].value.folders[1].value.documents", hasSize(1))
                .body("data.caseBundles[0].value.folders[1].value.documents[0].value.name",
                    equalTo("Single doc 1"));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Functional tests bundle 3"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testAddFilteredFolderedDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "testbundleconfiguration/f-tests-4-filtered-foldered-docs.yaml");
        json = findDocumentUrl(json);

        final ValidatableResponse response = postNewBundle(json);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders", hasSize(2))
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(2))
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(2))
                .body("data.caseBundles[0].value.folders[0].value.documents[0].value.name",
                    equalTo("Prosecution doc 1"))
                .body("data.caseBundles[0].value.folders[0].value.documents[1].value.name",
                    equalTo("Prosecution doc 2"))
                .body("data.caseBundles[0].value.folders[1].value.documents", hasSize(1))
                .body("data.caseBundles[0].value.folders[1].value.documents[0].value.name",
                    equalTo("Single doc 1"));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Functional tests bundle 4"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testTypoInConfigurationFile() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "testbundleconfiguration/f-tests-6-has-typo.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response
                .assertThat()
                .log()
                .all()
                .statusCode(400)
                .body("errors", contains("Invalid configuration file entry in: "
                        + "testbundleconfiguration/f-tests-6-has-typo.yaml; "
                        + "Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    void testDefaultFallBackConfigurationFile() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(400)
                .body("errors", contains("Invalid configuration file entry in: configurationFile;"
                    + " Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    void testDocumentNotPresent() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json
                .replaceAll(
                        "configurationFile",
                        "testbundleconfiguration/f-tests-12-invalid-document-property.yaml"
                );
        json = findDocumentUrl(json);

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(2));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Functional tests bundle 1"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testDocumentPropertyIsAnArray() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "testbundleconfiguration/f-tests-7-not-a-single-doc.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(400)
                .body("errors", contains("Element is an array: /caseDocuments"));
    }

    @Test
    void testDocumentSetPropertyIsNotAnArray() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "testbundleconfiguration/f-tests-8-not-an-array.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(400)
                .body("errors", contains("Element is not an array: /singleDocument"));
    }

    @Test
    void testDocumentStructureCorrupted() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("document_url", "incorrect_property_name");
        json = json.replaceAll("configurationFile", "testbundleconfiguration/f-tests-5-invalid-url.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(400)
                .body("errors", contains("Could not find the property /documentLink/document_url in the node: "));
    }

    @Test
    void testConfigurationFileDoesNotExist() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "nonexistent.yaml");

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(400)
                .body("errors", contains("Invalid configuration file entry in: nonexistent.yaml;"
                    + " Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    void testMultipleFilters() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "testbundleconfiguration/f-tests-9-multiple-filters.yaml");
        json = findDocumentUrl(json);

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders", hasSize(1))
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(3))
                .body("data.caseBundles[0].value.folders[0].value.documents[0].value.name",
                    equalTo("Prosecution doc 1"))
                .body("data.caseBundles[0].value.folders[0].value.documents[1].value.name",
                    equalTo("Prosecution doc 2"))
                .body("data.caseBundles[0].value.folders[0].value.documents[2].value.name",
                    equalTo("Evidence doc"));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Functional Test 11"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testSortDocumentsAscending() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "testbundleconfiguration/f-tests-10-sorting.yaml");
        json = findDocumentUrl(json);

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders", hasSize(2))
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(4))
                .body("data.caseBundles[0].value.folders[0].value.documents[0].value.name",
                    equalTo("Prosecution doc 1"))
                .body("data.caseBundles[0].value.folders[0].value.documents[1].value.name",
                    equalTo("Prosecution doc 2"))
                .body("data.caseBundles[0].value.folders[0].value.documents[2].value.name",
                    equalTo("Evidence doc"))
                .body("data.caseBundles[0].value.folders[0].value.documents[3].value.name",
                    equalTo("Defendant doc 1"))
                .body("data.caseBundles[0].value.folders[1].value.documents", hasSize(1))
                .body("data.caseBundles[0].value.folders[1].value.documents[0].value.name",
                    equalTo("Single doc 1"));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Functional tests bundle 3"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
     void testSortDocumentsDescending() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "testbundleconfiguration/f-tests-11-sorting.yaml");
        json = findDocumentUrl(json);

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders", hasSize(2))
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(4))
                .body("data.caseBundles[0].value.folders[0].value.documents[0].value.name",
                    equalTo("Defendant doc 1"))
                .body("data.caseBundles[0].value.folders[0].value.documents[1].value.name",
                    equalTo("Evidence doc"))
                .body("data.caseBundles[0].value.folders[0].value.documents[2].value.name",
                    equalTo("Prosecution doc 2"))
                .body("data.caseBundles[0].value.folders[0].value.documents[3].value.name",
                    equalTo("Prosecution doc 1"))
                .body("data.caseBundles[0].value.folders[1].value.documents", hasSize(1))
                .body("data.caseBundles[0].value.folders[1].value.documents[0].value.name",
                    equalTo("Single doc 1"));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Functional tests bundle 3"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testEnableEmailNotificationIsNull() {
        final ValidatableResponse response = postNewBundle(validJson);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.enableEmailNotification", nullValue());

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("New bundle"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testRenderImageInStitchedDocument() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "testbundleconfiguration/f-tests-13-render-image-flat-docs.yaml");
        json = findDocumentUrl(json);

        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.documentImage.docmosisAssetId", equalTo("hmcts.png"))
                .body("data.caseBundles[0].value.documentImage.imageRenderingLocation",
                    equalTo("allPages"))
                .body("data.caseBundles[0].value.documentImage.imageRendering", equalTo("opaque"))
                .body("data.caseBundles[0].value.documentImage.coordinateX", equalTo(50))
                .body("data.caseBundles[0].value.documentImage.coordinateY", equalTo(50));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Functional test For Image Rendering"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testRedactedDocuments() {
        String json = customDocumentsJson.toString();
        json = findDocumentUrl(json);
        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(4))
                .body("data.caseBundles[0].value.folders[0].value.documents[0].value.name",
                    equalTo("Non Redacted Doc1.pdf"))
                .body("data.caseBundles[0].value.folders[0].value.documents[1].value.name",
                    equalTo("Redacted Doc2.pdf"))
                .body("data.caseBundles[0].value.folders[0].value.documents[2].value.name",
                    equalTo("Redacted Doc3.pdf"))
                .body("data.caseBundles[0].value.folders[0].value.documents[3].value.name",
                    equalTo("AT38.png"));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Redacted Bundle"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testNonRedactedDocuments() {
        String json = nonCustomDocumentsJson.toString();
        json = findDocumentUrl(json);
        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders[0].value.documents", hasSize(3))
                .body("data.caseBundles[0].value.folders[0].value.documents[0].value.name",
                    equalTo("Non Redacted Doc1.pdf"))
                .body("data.caseBundles[0].value.folders[0].value.documents[1].value.name",
                    equalTo("DWP response.pdf"))
                .body("data.caseBundles[0].value.folders[0].value.documents[2].value.name",
                    equalTo("DWP evidence.pdf"));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Redacted Bundle"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void testMultiBundleDocuments() {

        String json = multiBundleDocumentsJson.toString();
        json = findDocumentUrl(json);
        final ValidatableResponse response = postNewBundle(json);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles", hasSize(2));

        long documentTaskId = response.extract().body().jsonPath().getLong("documentTaskId");
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body("bundle.bundleTitle", equalTo("Redacted Bundle"))
                .body("bundle.stitchedDocumentURI", notNullValue());
    }

    @Test
    void shouldReturn401WhenUnAuthenticatedUserCreateBundle() {
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
