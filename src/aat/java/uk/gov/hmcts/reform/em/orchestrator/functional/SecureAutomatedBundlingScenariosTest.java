package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.em.orchestrator.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

import java.io.IOException;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class SecureAutomatedBundlingScenariosTest extends BaseTest {

    public static final String CONFIGURATION_FILE = "configurationFile";
    public static final String ERRORS = "errors";
    public static final String SINGLE_DOC_1 = "Single doc 1";
    public static final String EVIDENCE_DOC = "Evidence doc";
    public static final String PROSECUTION_DOC_2 = "Prosecution doc 2";
    public static final String PROSECUTION_DOC_1 = "Prosecution doc 1";
    public static final String DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_1_VALUE_NAME =
            "data.caseBundles[0].value.folders[0].value.documents[1].value.name";
    public static final String DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_0_VALUE_NAME =
            "data.caseBundles[0].value.folders[0].value.documents[0].value.name";
    public static final String DEFENDANT_DOC_1 = "Defendant doc 1";
    public static final String DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_2_VALUE_NAME =
            "data.caseBundles[0].value.folders[0].value.documents[2].value.name";
    public static final String DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_3_VALUE_NAME =
            "data.caseBundles[0].value.folders[0].value.documents[3].value.name";
    public static final String DATA_CASE_BUNDLES_0_VALUE_FOLDERS_1_VALUE_DOCUMENTS_0_VALUE_NAME =
            "data.caseBundles[0].value.folders[1].value.documents[0].value.name";
    public static final String DATA_CASE_BUNDLES_0_VALUE_FOLDERS_1_VALUE_DOCUMENTS =
            "data.caseBundles[0].value.folders[1].value.documents";
    public static final String SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH =
            "src/aat/resources/documents-case.json";
    public static final String BUNDLE_WITH_FILENAME = "Bundle with filename";
    public static final String BUNDLE_TITLE = "Functional tests bundle 3";
    public static final String BUNDLE_BUNDLE_TITLE = "bundle.bundleTitle";
    public static final String DATA_CASE_BUNDLES_0_VALUE_FOLDERS = "data.caseBundles[0].value.folders";
    public static final String BUNDLE_STITCHED_DOCUMENT_URI = "bundle.stitchedDocumentURI";
    public static final String NEW_BUNDLE = "New bundle";
    public static final String DOCUMENT_TASK_ID = "documentTaskId";
    public static final String DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS =
            "data.caseBundles[0].value.folders[0].value.documents";
    public static final String REDACTED_BUNDLE = "Redacted Bundle";

    private JsonNode validJson;
    private JsonNode invalidJson;
    private JsonNode missingPropertiesJson;
    private JsonNode filenameJson;
    private JsonNode invalidConfigJson;
    private JsonNode customDocumentsJson;
    private JsonNode nonCustomDocumentsJson;
    private JsonNode multiBundleDocumentsJson;


    private RequestSpecification request;
    private RequestSpecification unAuthenticatedRequest;

    @Autowired
    protected SecureAutomatedBundlingScenariosTest(
            TestUtil testUtil,
            ExtendedCcdHelper extendedCcdHelper
    ) {
        super(testUtil, extendedCcdHelper);
    }

    @BeforeEach
    public void setup() throws IOException {
        assumeTrue(enableCdamValidation);
        validJson = extendedCcdHelper.loadCaseFromFile("automated-case.json");
        invalidJson = extendedCcdHelper.loadCaseFromFile("invalid-automated-case.json");
        missingPropertiesJson = extendedCcdHelper.loadMissingPropertiesCase("missing-cdam-properties-case.json");
        filenameJson = extendedCcdHelper.loadCaseFromFile("filename-case.json");
        invalidConfigJson = extendedCcdHelper.loadCaseFromFile("automated-case-invalid-configuration.json");
        customDocumentsJson = extendedCcdHelper.loadCaseFromFile("custom-documents-case.json");
        nonCustomDocumentsJson = extendedCcdHelper.loadCaseFromFile("non-custom-documents-case.json");
        multiBundleDocumentsJson = extendedCcdHelper.loadCaseFromFile("multi-bundle-case.json");
        setupRequests();
    }

    @Test
    void testCreateBundle() {
        String cdamJson = testUtil.addCdamProperties(validJson);
        final ValidatableResponse response = postNewBundle(cdamJson);
        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo(NEW_BUNDLE))
                .body("data.caseBundles[0].value.folders[0].value.name", equalTo("Folder 1"))
                .body("data.caseBundles[0].value.folders[0].value.folders[0].value.name",
                    equalTo("Folder 1.a"))
                .body("data.caseBundles[0].value.folders[0].value.folders[1].value.name",
                    equalTo("Folder 1.b"))
                .body("data.caseBundles[0].value.folders[1].value.name", equalTo("Folder 2"))
                .body("data.caseBundles[0].value.fileName", equalTo("stitched.pdf"));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo(NEW_BUNDLE))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testMissingCdamProperties() {
        final ValidatableResponse response = postNewBundle(missingPropertiesJson);
        response
                .assertThat().log().all()
                .statusCode(400)
                .body(ERRORS, containsInAnyOrder("caseTypeId or case_type_id is required attribute",
                        "jurisdictionId or jurisdiction is required attribute"));

    }

    @Test
    void testInvalidConfig() {
        String cdamJson = testUtil.addCdamProperties(invalidJson);
        final ValidatableResponse response = postNewBundle(cdamJson);
        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", equalTo("Invalid configuration file entry in: does-not-exist.yaml"
                        + "; Configuration file parameter(s) and/or parameter value(s)"));

    }

    @Test
    void testCorruptConfig() {
        String cdamJson = testUtil.addCdamProperties(invalidConfigJson);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response
                .assertThat().log().all()
                .statusCode(400)
                .body("errors[0]", equalTo("Invalid configuration file entry in:"
                    + " testbundleconfiguration/example-incorrect-key.yaml;"
                    + " Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    void testFilename() {
        String cdamJson = testUtil.addCdamProperties(filenameJson);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.title", equalTo(BUNDLE_WITH_FILENAME))
                .body("data.caseBundles[0].value.fileName", equalTo("bundle.pdf"));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo(BUNDLE_WITH_FILENAME))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testTableOfContentsAndCoversheet() {
        String cdamJson = testUtil.addCdamProperties(validJson);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.hasCoversheets", equalTo("Yes"))
                .body("data.caseBundles[0].value.hasTableOfContents", equalTo("Yes"))
                .body("data.caseBundles[0].value.hasFolderCoversheets", equalTo("No"));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo(NEW_BUNDLE))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testFolderCoversheets() {
        String cdamJson = testUtil.addCdamProperties(filenameJson);
        final ValidatableResponse response = postNewBundle(cdamJson);
        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.hasCoversheets", equalTo("No"))
                .body("data.caseBundles[0].value.hasTableOfContents", equalTo("No"))
                .body("data.caseBundles[0].value.hasFolderCoversheets", equalTo("Yes"));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat()
                .log()
                .all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo(BUNDLE_WITH_FILENAME))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testSubSubfolders() {
        String cdamJson = testUtil.addCdamProperties(validJson);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.folders[0].value.folders[0].value.folders", nullValue())
                .body("data.caseBundles[0].value.folders[0].value.folders[0].value.documents", notNullValue());

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat()
                .log()
                .all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo(NEW_BUNDLE))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testAddFlatDocuments() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);
        json = json
                .replace(CONFIGURATION_FILE, "testbundleconfiguration/f-tests-1-flat-docs.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);


        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.documents", hasSize(4))
                .body("data.caseBundles[0].value.documents[0].value.name", equalTo(PROSECUTION_DOC_1))
                .body("data.caseBundles[0].value.documents[1].value.name", equalTo(PROSECUTION_DOC_2))
                .body("data.caseBundles[0].value.documents[2].value.name", equalTo(EVIDENCE_DOC))
                .body("data.caseBundles[0].value.documents[3].value.name", equalTo(DEFENDANT_DOC_1));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo("Functional tests bundle 1"))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());

    }

    @Test
    void testAddFlatFilteredDocuments() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);
        json = json
                .replace(CONFIGURATION_FILE, "testbundleconfiguration/f-tests-2-filter-flat-docs.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.documents", hasSize(2))
                .body("data.caseBundles[0].value.documents[0].value.name", equalTo(PROSECUTION_DOC_1))
                .body("data.caseBundles[0].value.documents[1].value.name", equalTo(PROSECUTION_DOC_2));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat()
                .log()
                .all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo("Functional tests bundle 2"))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testAddFolderedDocuments() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);
        json = json
                .replace(CONFIGURATION_FILE, "testbundleconfiguration/f-tests-3-foldered-docs.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS, hasSize(2))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS, hasSize(4))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_0_VALUE_NAME,
                    equalTo(PROSECUTION_DOC_1))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_1_VALUE_NAME,
                    equalTo(PROSECUTION_DOC_2))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_2_VALUE_NAME,
                    equalTo(DEFENDANT_DOC_1))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_3_VALUE_NAME,
                    equalTo(EVIDENCE_DOC))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_1_VALUE_DOCUMENTS, hasSize(1))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_1_VALUE_DOCUMENTS_0_VALUE_NAME,
                    equalTo(SINGLE_DOC_1));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat()
                .log()
                .all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo(BUNDLE_TITLE))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testAddFilteredFolderedDocuments() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);
        json = json.replace(CONFIGURATION_FILE,
                "testbundleconfiguration/f-tests-4-filtered-foldered-docs.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response
                .assertThat().log().all()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS, hasSize(2))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS, hasSize(2))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS, hasSize(2))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_0_VALUE_NAME,
                    equalTo(PROSECUTION_DOC_1))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_1_VALUE_NAME,
                    equalTo(PROSECUTION_DOC_2))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_1_VALUE_DOCUMENTS, hasSize(1))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_1_VALUE_DOCUMENTS_0_VALUE_NAME,
                    equalTo(SINGLE_DOC_1));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat()
                .log()
                .all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo("Functional tests bundle 4"))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testTypoInConfigurationFile() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);
        json = json.replace(CONFIGURATION_FILE, "testbundleconfiguration/f-tests-6-has-typo.yaml");

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response
                .assertThat()
                .log()
                .all()
                .statusCode(400)
                .body(ERRORS, contains("Invalid configuration file entry in: "
                        + "testbundleconfiguration/f-tests-6-has-typo.yaml; "
                        + "Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    void testDefaultFallBackConfigurationFile() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response
                .assertThat()
                .log()
                .all()
                .statusCode(400)
                .body(ERRORS, contains("Invalid configuration file entry in: configurationFile;"
                        + " Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    void testDocumentNotPresent() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);
        json = json
                .replace(CONFIGURATION_FILE,
                        "testbundleconfiguration/f-tests-12-invalid-document-property.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS, hasSize(2));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo("Functional tests bundle 1"))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testDocumentPropertyIsAnArray() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);
        json = json.replace(CONFIGURATION_FILE, "testbundleconfiguration/f-tests-7-not-a-single-doc.yaml");

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(400)
                .body(ERRORS, contains("Element is an array: /caseDocuments"));
    }

    @Test
    void testDocumentSetPropertyIsNotAnArray() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);
        json = json.replace(CONFIGURATION_FILE, "testbundleconfiguration/f-tests-8-not-an-array.yaml");

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(400)
                .body(ERRORS, contains("Element is not an array: /singleDocument"));
    }

    @Test
    void testDocumentStructureCorrupted() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);
        json = json.replace("document_url", "incorrect_property_name");
        json = json.replace(CONFIGURATION_FILE, "testbundleconfiguration/f-tests-5-invalid-url.yaml");

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(400)
                .body(ERRORS, contains("Could not find the property /documentLink/document_url in the node: "));
    }

    @Test
    void testConfigurationFileDoesNotExist() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);
        json = json.replace(CONFIGURATION_FILE, "nonexistent.yaml");

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response
                .assertThat()
                .log()
                .all()
                .statusCode(400)
                .body(ERRORS, contains("Invalid configuration file entry in: nonexistent.yaml;"
                        + " Configuration file parameter(s) and/or parameter value(s)"));
    }

    @Test
    void testMultipleFilters() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);
        json = json.replace(CONFIGURATION_FILE, "testbundleconfiguration/f-tests-9-multiple-filters.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS, hasSize(1))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS, hasSize(3))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_0_VALUE_NAME,
                    equalTo(PROSECUTION_DOC_1))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_1_VALUE_NAME,
                    equalTo(PROSECUTION_DOC_2))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_2_VALUE_NAME,
                    equalTo(EVIDENCE_DOC));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo("Functional Test 11"))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testSortDocumentsAscending() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);
        json = json.replace(CONFIGURATION_FILE, "testbundleconfiguration/f-tests-10-sorting.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS, hasSize(2))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS, hasSize(4))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_0_VALUE_NAME,
                    equalTo(PROSECUTION_DOC_1))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_1_VALUE_NAME,
                    equalTo(PROSECUTION_DOC_2))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_2_VALUE_NAME,
                    equalTo(EVIDENCE_DOC))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_3_VALUE_NAME,
                    equalTo(DEFENDANT_DOC_1))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_1_VALUE_DOCUMENTS, hasSize(1))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_1_VALUE_DOCUMENTS_0_VALUE_NAME,
                    equalTo(SINGLE_DOC_1));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo(BUNDLE_TITLE))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testSortDocumentsDescending() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);
        json = json.replace(CONFIGURATION_FILE, "testbundleconfiguration/f-tests-11-sorting.yaml");
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS, hasSize(2))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS, hasSize(4))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_0_VALUE_NAME,
                    equalTo(DEFENDANT_DOC_1))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_1_VALUE_NAME,
                    equalTo(EVIDENCE_DOC))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_2_VALUE_NAME,
                    equalTo(PROSECUTION_DOC_2))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_3_VALUE_NAME,
                    equalTo(PROSECUTION_DOC_1))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_1_VALUE_DOCUMENTS, hasSize(1))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_1_VALUE_DOCUMENTS_0_VALUE_NAME,
                    equalTo(SINGLE_DOC_1));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo(BUNDLE_TITLE))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testEnableEmailNotificationIsNull() {
        String cdamJson = testUtil.addCdamProperties(validJson);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles[0].value.enableEmailNotification", nullValue());

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo(NEW_BUNDLE))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testRenderImageInStitchedDocument() throws IOException {
        String json = TestUtil.readFile(SRC_AAT_RESOURCES_DOCUMENTS_CASE_JSON_FILE_PATH);
        json = json.replace(CONFIGURATION_FILE, "testbundleconfiguration/f-tests-13-render-image-flat-docs.yaml");
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

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo("Functional test For Image Rendering"))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testRedactedDocuments() {
        String json = customDocumentsJson.toString();
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS, hasSize(4))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_0_VALUE_NAME,
                    equalTo("Non Redacted Doc1.pdf"))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_1_VALUE_NAME,
                    equalTo("Redacted Doc2.pdf"))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_2_VALUE_NAME,
                    equalTo("Redacted Doc3.pdf"))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_3_VALUE_NAME,
                    equalTo("AT38.png"));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo(REDACTED_BUNDLE))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testNonRedactedDocuments() {
        String json = nonCustomDocumentsJson.toString();
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);

        response.assertThat()
                .log().all()
                .statusCode(200)
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS, hasSize(3))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_0_VALUE_NAME,
                    equalTo("Non Redacted Doc1.pdf"))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_1_VALUE_NAME,
                    equalTo("DWP response.pdf"))
                .body(DATA_CASE_BUNDLES_0_VALUE_FOLDERS_0_VALUE_DOCUMENTS_2_VALUE_NAME,
                    equalTo("DWP evidence.pdf"));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo(REDACTED_BUNDLE))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
    }

    @Test
    void testMultiBundleDocuments() {

        String json = multiBundleDocumentsJson.toString();
        json = findDocumentUrl(json);

        String cdamJson = testUtil.addCdamProperties(json);
        final ValidatableResponse response = postNewBundle(cdamJson);
        response.assertThat()
                .log().all()
                .statusCode(200)
                .body("data.caseBundles", hasSize(2));

        long documentTaskId = response.extract().body().jsonPath().getLong(DOCUMENT_TASK_ID);
        final ValidatableResponse pollResponse = testUtil.poll(documentTaskId);
        pollResponse
                .assertThat().log().all()
                .statusCode(200)
                .body(BUNDLE_BUNDLE_TITLE, equalTo(REDACTED_BUNDLE))
                .body(BUNDLE_STITCHED_DOCUMENT_URI, notNullValue());
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
                .cdamAuthRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE);

        unAuthenticatedRequest = testUtil
                .unauthenticatedRequest()
                .baseUri(testUtil.getTestUrl())
                .contentType(APPLICATION_JSON_VALUE);
    }

    @NotNull
    private String findDocumentUrl(String json) {
        Document.Links links = testUtil.uploadCdamDocument();

        json = json.replace(
                "\"document_url\":\"documentUrl\"",
                String.format("\"document_url\":\"%s\"", links.self.href)
        );
        json = json.replace(
                "\"document_binary_url\":\"documentUrl",
                String.format("\"document_binary_url\":\"%s", links.binary.href)
        );
        return json;
    }

}
