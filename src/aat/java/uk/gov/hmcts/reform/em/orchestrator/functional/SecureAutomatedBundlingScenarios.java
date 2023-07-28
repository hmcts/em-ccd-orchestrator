package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.jetbrains.annotations.NotNull;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.io.IOException;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class SecureAutomatedBundlingScenarios extends BaseTest {

    private static JsonNode validJson;
    private static JsonNode invalidJson;
    private static JsonNode missingPropertiesJson;
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
        Assume.assumeTrue(enableCdamValidation);
        validJson = extendedCcdHelper.loadCaseFromFile("automated-case.json");
        invalidJson = extendedCcdHelper.loadCaseFromFile("invalid-automated-case.json");
        missingPropertiesJson = extendedCcdHelper.loadMissingPropertiesCase("missing-cdam-properties-case.json");
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
        String cdamJson = testUtil.addCdamProperties(validJson);
        final ValidatableResponse response = postNewBundle(cdamJson);
        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo("New bundle"))
                .body("data.caseBundles[0].value.folders[0].value.name", equalTo("Folder 1"))
                .body("data.caseBundles[0].value.folders[0].value.folders[0].value.name",
                    equalTo("Folder 1.a"))
                .body("data.caseBundles[0].value.folders[0].value.folders[1].value.name",
                    equalTo("Folder 1.b"))
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
    public void testMissingCdamProperties() {
        final ValidatableResponse response = postNewBundle(missingPropertiesJson);
        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors", containsInAnyOrder("caseTypeId or case_type_id is required attribute",
                        "jurisdictionId or jurisdiction is required attribute"));

    }

    @Test
    public void testInvalidConfig() {
        String cdamJson = testUtil.addCdamProperties(invalidJson);
        final ValidatableResponse response = postNewBundle(cdamJson);
        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", equalTo("Invalid configuration file entry in: does-not-exist.yaml"
                        + "; Configuration file parameter(s) and/or parameter value(s)"));

    }

    @Test
    public void testCorruptConfig() {
        String cdamJson = testUtil.addCdamProperties(invalidConfigJson);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", equalTo("Invalid configuration file entry in:"
                    + " example-incorrect-key.yaml; Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    public void testFilename() throws IOException, InterruptedException {
        String cdamJson = testUtil.addCdamProperties(filenameJson);
        final ValidatableResponse response = postNewBundle(cdamJson);

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
    public void testTableOfContentsAndCoversheet() throws IOException, InterruptedException {
        String cdamJson = testUtil.addCdamProperties(validJson);
        final ValidatableResponse response = postNewBundle(cdamJson);

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
    public void testFolderCoversheets() throws IOException, InterruptedException {
        String cdamJson = testUtil.addCdamProperties(filenameJson);
        final ValidatableResponse response = postNewBundle(cdamJson);
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
    public void testSubSubfolders() throws IOException, InterruptedException {
        String cdamJson = testUtil.addCdamProperties(validJson);
        final ValidatableResponse response = postNewBundle(cdamJson);

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
    public void testAddFlatDocuments() throws Exception {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-1-flat-docs.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);


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
    public void testAddFlatFilteredDocuments() throws Exception {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-2-filter-flat-docs.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

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
    public void testAddFolderedDocuments() throws Exception {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-3-foldered-docs.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

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
    public void testAddFilteredFolderedDocuments() throws Exception {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-4-filtered-foldered-docs.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

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
    public void testTypoInConfigurationFile() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-6-has-typo.yaml");

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(400)
                .body("errors", contains("Invalid configuration file entry in: f-tests-6-has-typo.yaml; "
                        + "Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    public void testDefaultFallBackConfigurationFile() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(400)
                .body("errors", contains("Invalid configuration file entry in: configurationFile;"
                    + " Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    public void testDocumentNotPresent() throws Exception {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-12-invalid-document-property.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

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
    public void testDocumentPropertyIsAnArray() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-7-not-a-single-doc.yaml");

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(400)
                .body("errors", contains("Element is an array: /caseDocuments"));
    }

    @Test
    public void testDocumentSetPropertyIsNotAnArray() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-8-not-an-array.yaml");

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(400)
                .body("errors", contains("Element is not an array: /singleDocument"));
    }

    @Test
    public void testDocumentStructureCorrupted() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("document_url", "incorrect_property_name");
        json = json.replaceAll("configurationFile", "f-tests-5-invalid-url.yaml");

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(400)
                .body("errors", contains("Could not find the property /documentLink/document_url in the node: "));
    }

    @Test
    public void testConfigurationFileDoesNotExist() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "nonexistent.yaml");

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(400)
                .body("errors", contains("Invalid configuration file entry in: nonexistent.yaml;" +
                    " Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    public void testMultipleFilters() throws Exception {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-9-multiple-filters.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

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
    public void testSortDocumentsAscending() throws Exception {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-10-sorting.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

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
    public void testSortDocumentsDescending() throws Exception {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-11-sorting.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

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
    public void testEnableEmailNotificationIsNull() throws IOException, InterruptedException {
        String cdamJson = testUtil.addCdamProperties(validJson);
        final ValidatableResponse response = postNewBundle(cdamJson);

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
    public void testRenderImageInStitchedDocument() throws Exception {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-13-render-image-flat-docs.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.documentImage.docmosisAssetId",
                    equalTo("hmcts.png"))
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
    public void testRedactedDocuments() throws Exception {
        String json = customDocumentsJson.toString();
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

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
    public void testNonRedactedDocuments() throws Exception {
        String json = nonCustomDocumentsJson.toString();
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

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
    public void testMultiBundleDocuments() throws Exception {

        String json = multiBundleDocumentsJson.toString();
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);
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
                .cdamAuthRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE);

        unAuthenticatedRequest = testUtil
                .unauthenticatedRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE);
    }

    @NotNull
    private String findDocumentUrl(String json) throws Exception {
        Document.Links links = testUtil.uploadCdamDocument();

        json = json.replaceAll(
                "\"document_url\":\"documentUrl\"",
                String.format("\"document_url\":\"%s\"", links.self.href)
        );
        json = json.replaceAll(
                "\"document_binary_url\":\"documentUrl",
                String.format("\"document_binary_url\":\"%s", links.binary.href)
        );
        return json;
    }

}
