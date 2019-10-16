package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.em.orchestrator.testutil.Env;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AutomatedBundlingScenarios {

    private static final TestUtil testUtil = new TestUtil();
    private static JsonNode validJson;
    private static JsonNode invalidJson;
    private static JsonNode filenameJson;

    @BeforeClass
    public static void setup() throws Exception {
        testUtil.getCcdHelper().importCcdDefinitionFile();
        validJson = testUtil.getCcdHelper().loadCaseFromFile("automated-case.json");
        invalidJson = testUtil.getCcdHelper().loadCaseFromFile("invalid-automated-case.json");
        filenameJson = testUtil.getCcdHelper().loadCaseFromFile("filename-case.json");
    }

    @Test
    public void testCreateBundle() {
        Response response = testUtil.authRequest()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(validJson)
            .request("POST", Env.getTestUrl() + "/api/new-bundle");

        assertEquals(200, response.getStatusCode());
        assertEquals("New bundle", response.getBody().jsonPath().getString("data.caseBundles[0].value.title"));
        assertEquals("Folder 1", response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.name"));
        assertEquals("Folder 1.a", response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.folders[0].value.name"));
        assertEquals("Folder 1.b", response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.folders[1].value.name"));
        assertEquals("Folder 2", response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[1].value.name"));
        assertEquals("stitched.pdf", response.getBody().jsonPath().getString("data.caseBundles[0].value.fileName"));
    }

    @Test
    public void testInvalidConfig() {
        Response response = testUtil.authRequest()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(invalidJson)
            .request("POST", Env.getTestUrl() + "/api/new-bundle");

        assertEquals(200, response.getStatusCode());
        assertEquals("Unable to load configuration: does-not-exist.yaml", response.getBody().jsonPath().getString("errors[0]"));
    }

    @Test
    public void testFilename() {
        Response response = testUtil.authRequest()
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .body(filenameJson)
            .request("POST", Env.getTestUrl() + "/api/new-bundle");

        assertEquals(200, response.getStatusCode());
        assertEquals("Bundle with filename", response.getBody().jsonPath().getString("data.caseBundles[0].value.title"));
        assertEquals("bundle.pdf", response.getBody().jsonPath().getString("data.caseBundles[0].value.fileName"));
    }

    @Test
    public void testTableOfContentsAndCoversheet() {
        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(validJson)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        assertEquals(200, response.getStatusCode());
        assertEquals("Yes", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasCoversheets"));
        assertEquals("Yes", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasTableOfContents"));
        assertEquals("No", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasFolderCoversheets"));
    }

    @Test
    public void testFolderCoversheets() {
        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(filenameJson)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        assertEquals(200, response.getStatusCode());
        assertEquals("No", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasCoversheets"));
        assertEquals("No", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasTableOfContents"));
        assertEquals("Yes", response.getBody().jsonPath().getString("data.caseBundles[0].value.hasFolderCoversheets"));
    }

    @Test
    public void testSubSubfolders() {
        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(validJson)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        assertEquals(200, response.getStatusCode());
        Assert.assertNull(response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.folders[0].value.folders"));
        Assert.assertNotNull(response.getBody().jsonPath().getString("data.caseBundles[0].value.folders[0].value.folders[0].value.documents"));
    }

    @Test
    public void testAddFlatDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-1-flat-docs.yaml");

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        JsonPath responsePath = response.jsonPath();

        assertEquals(200, response.getStatusCode());
        assertEquals(3, responsePath.getList("data.caseBundles[0].value.documents").size());
        assertEquals("Prosecution doc 1", responsePath.getString("data.caseBundles[0].value.documents[0].value.name"));
        assertEquals("Prosecution doc 2", responsePath.getString("data.caseBundles[0].value.documents[1].value.name"));
        assertEquals("Defendant doc 1", responsePath.getString("data.caseBundles[0].value.documents[2].value.name"));
    }

    @Test
    public void testAddFlatFilteredDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-2-filter-flat-docs.yaml");

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        JsonPath responsePath = response.jsonPath();

        assertEquals(200, response.getStatusCode());
        assertEquals(2, responsePath.getList("data.caseBundles[0].value.documents").size());
        assertEquals("Prosecution doc 1", responsePath.getString("data.caseBundles[0].value.documents[0].value.name"));
        assertEquals("Prosecution doc 2", responsePath.getString("data.caseBundles[0].value.documents[1].value.name"));
    }

    @Test
    public void testAddFolderedDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-3-foldered-docs.yaml");

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        JsonPath responsePath = response.jsonPath();

        assertEquals(200, response.getStatusCode());
        assertEquals(2, responsePath.getList("data.caseBundles[0].value.folders").size());
        assertEquals(3, responsePath.getList("data.caseBundles[0].value.folders[0].value.documents").size());
        assertEquals("Prosecution doc 1", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[0].value.name"));
        assertEquals("Prosecution doc 2", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[1].value.name"));
        assertEquals("Defendant doc 1", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[2].value.name"));
        assertEquals(1, responsePath.getList("data.caseBundles[0].value.folders[1].value.documents").size());
        assertEquals("Single doc 1", responsePath.getString("data.caseBundles[0].value.folders[1].value.documents[0].value.name"));
    }

    @Test
    public void testAddFilteredFolderedDocuments() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-4-filtered-foldered-docs.yaml");

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        JsonPath responsePath = response.jsonPath();

        assertEquals(200, response.getStatusCode());
        assertEquals(2, responsePath.getList("data.caseBundles[0].value.folders").size());
        assertEquals(2, responsePath.getList("data.caseBundles[0].value.folders[0].value.documents").size());
        assertEquals("Prosecution doc 1", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[0].value.name"));
        assertEquals("Prosecution doc 2", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[1].value.name"));
        assertEquals(1, responsePath.getList("data.caseBundles[0].value.folders[1].value.documents").size());
        assertEquals("Single doc 1", responsePath.getString("data.caseBundles[0].value.folders[1].value.documents[0].value.name"));
    }

    @Test
    public void testTypoInConfigurationFile() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-6-has-typo.yaml");

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testDocumentPropertyIsAnArray() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-7-not-a-single-doc.yaml");

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        assertTrue(response.prettyPrint().contains("Element is an array: /caseDocuments"));
    }

    @Test
    public void testDocumentSetPropertyIsNotAnArray() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-8-not-an-array.yaml");

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        assertTrue(response.prettyPrint().contains("Element is not an array: /singleDocument"));
    }

    @Test
    public void testInvalidDocumentProperty() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-9-invalid-doc-property.yaml");

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        assertTrue(response.prettyPrint().contains("Could not find element: /typoDocument"));
    }

    @Test
    public void testInvalidDocumentSetProperty() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-10-invalid-doc-set-property.yaml");

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        assertTrue(response.prettyPrint().contains("Could not find element: /quesoDocument"));
    }

    @Test
    public void testDocumentStructureCorrupted() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("document_url", "incorrect_property_name");
        json = json.replaceAll("configurationFile", "f-tests-5-invalid-url.yaml");

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        assertTrue(response.prettyPrint().contains("Could not find the property /documentLink/document_url in the node"));
    }

    @Test
    public void testConfigurationFileDoesNotExist() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "nonexistent.yaml");

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        assertTrue(response.getBody().print().contains("Unable to load configuration: nonexistent.yaml"));
    }

    @Test
    public void testMultipleFilters() throws IOException {
        String json = TestUtil.readFile("src/aat/resources/documents-case.json");
        json = json.replaceAll("configurationFile", "f-tests-11-multiple-filters.yaml");

        Response response = testUtil.authRequest()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .request("POST", Env.getTestUrl() + "/api/new-bundle");

        JsonPath responsePath = response.jsonPath();

        assertEquals(200, response.getStatusCode());
        assertEquals(1, responsePath.getList("data.caseBundles[0].value.folders").size());
        assertEquals(3, responsePath.getList("data.caseBundles[0].value.folders[0].value.documents").size());
        assertEquals("Prosecution doc 1", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[0].value.name"));
        assertEquals("Prosecution doc 2", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[1].value.name"));
        assertEquals("Evidence doc", responsePath.getString("data.caseBundles[0].value.folders[0].value.documents[2].value.name"));
    }
}
