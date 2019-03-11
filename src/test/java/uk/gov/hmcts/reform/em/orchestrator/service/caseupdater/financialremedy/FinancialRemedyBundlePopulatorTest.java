package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.financialremedy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.financialremedyservice.FinancialRemedyBundlePopulator;

import static org.junit.Assert.assertEquals;

public class FinancialRemedyBundlePopulatorTest {

    ObjectMapper objectMapper = new ObjectMapper();

    FinancialRemedyBundlePopulator financialRemedyBundlePopulator = new FinancialRemedyBundlePopulator(objectMapper);

    @Test
    public void populateNewBundleWithAllDocuments() throws Exception {
        JSONObject data = new JSONObject();
        data.put("uploadScanned4AForm", prepareDocument());
        data.put("uploadScanned4BForm", prepareDocument());
        data.put("uploadAdditionalDocument", prepareCollectionOfDocuments());
        JsonNode jsonNode = financialRemedyBundlePopulator
                .populateNewBundle(objectMapper.readTree(data.toString(4)));
        assertEquals("New Bundle", jsonNode.at("/value/title").asText());
        assertEquals("doc_url",
                jsonNode.at("/value/documents").get(0).at("/value/sourceDocument/document_url").asText());
        assertEquals("doc_fn",
                jsonNode.at("/value/documents").get(0).at("/value/sourceDocument/document_filename").asText());
        assertEquals("doc_b_url",
                jsonNode.at("/value/documents").get(0).at("/value/sourceDocument/document_binary_url").asText());
    }

    @Test
    public void populateNewBundleWithFewDocuments() throws Exception {
        JSONObject data = new JSONObject();
        data.put("uploadScanned4AForm", prepareDocument("4AForm_url", "4AForm_fn", "4AForm_b_url"));
        data.put("uploadScanned4BForm", prepareDocument("4BForm_url", "4BForm_fn", "4BForm_b_url"));
        data.put("miniFormA", prepareDocument("miniForm_url", "miniForm_fn", "miniForm_b_url"));
        JsonNode jsonNode = financialRemedyBundlePopulator
                .populateNewBundle(objectMapper.readTree(data.toString()));
        assertEquals("New Bundle", jsonNode.at("/value/title").asText());
        assertEquals("4AForm_url",
                jsonNode.at("/value/documents").get(0).at("/value/sourceDocument/document_url").asText());
        assertEquals("4AForm_fn",
                jsonNode.at("/value/documents").get(0).at("/value/sourceDocument/document_filename").asText());
        assertEquals("4AForm_b_url",
                jsonNode.at("/value/documents").get(0).at("/value/sourceDocument/document_binary_url").asText());
        assertEquals("4BForm_url",
                jsonNode.at("/value/documents").get(1).at("/value/sourceDocument/document_url").asText());
        assertEquals("miniForm_url",
                jsonNode.at("/value/documents").get(2).at("/value/sourceDocument/document_url").asText());
    }

    @Test
    public void populateNewBundleWithCollectionDocuments() throws Exception {
        JSONObject data = new JSONObject();
        data.put("uploadAdditionalDocument", prepareCollectionOfDocuments());
        JsonNode jsonNode = financialRemedyBundlePopulator
                .populateNewBundle(objectMapper.readTree(data.toString(4)));
        assertEquals("New Bundle", jsonNode.at("/value/title").asText());
        assertEquals("add1Form_url",
                jsonNode.at("/value/documents").get(0).at("/value/sourceDocument/document_url").asText());
        assertEquals("add1Form_fn",
                jsonNode.at("/value/documents").get(0).at("/value/sourceDocument/document_filename").asText());
        assertEquals("add1Form_b_url",
                jsonNode.at("/value/documents").get(0).at("/value/sourceDocument/document_binary_url").asText());
        assertEquals("add2Form_b_url",
                jsonNode.at("/value/documents").get(1).at("/value/sourceDocument/document_binary_url").asText());
    }

    @Test
    public void populateNewEmptyBundle() {
        JsonNode jsonNode = financialRemedyBundlePopulator
                .populateNewBundle(objectMapper.createObjectNode());
        assertEquals("New Bundle", jsonNode.at("/value/title").asText());
    }


    private static JSONArray prepareCollectionOfDocuments() {
        JSONArray jsonArray = new JSONArray();
        JSONObject value1 = new JSONObject();
        JSONObject document1 = new JSONObject();
        document1.put("additionalDocuments", prepareDocument("add1Form_url", "add1Form_fn", "add1Form_b_url"));
        jsonArray.put(value1.put("value", document1));
        JSONObject value2 = new JSONObject();
        JSONObject document2 = new JSONObject();
        document2.put("additionalDocuments", prepareDocument("add2Form_url", "add2Form_fn", "add2Form_b_url"));
        jsonArray.put(value2.put("value", document2));
        return jsonArray;
    }

    private static JSONObject prepareDocument(String... args) {
        JSONObject document = new JSONObject();
        document.put("document_url", args.length > 0 ? args[0] : "doc_url");
        document.put("document_filename", args.length > 1 ? args[1] : "doc_fn");
        document.put("document_binary_url", args.length > 2 ? args[2] : "doc_b_url");
        return document;
    }
}
