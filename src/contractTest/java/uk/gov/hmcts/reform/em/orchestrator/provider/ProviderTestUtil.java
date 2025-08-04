package uk.gov.hmcts.reform.em.orchestrator.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import java.util.UUID;

public final class ProviderTestUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ProviderTestUtil() {
    }

    public static CcdCallbackResponseDto createSyncStitchResponse() {
        CcdCallbackResponseDto response = new CcdCallbackResponseDto();
        ObjectNode caseData = MAPPER.createObjectNode();
        ArrayNode caseBundles = caseData.putArray("caseBundles");

        ObjectNode bundle = buildCcdBundle();
        bundle.set("stitchedDocument", buildCcdDocument());

        caseBundles.add(MAPPER.createObjectNode().set("value", bundle));
        response.setData(caseData);
        return response;
    }

    public static CcdCallbackResponseDto createAsyncStitchResponse() {
        CcdCallbackResponseDto response = new CcdCallbackResponseDto();
        response.setDocumentTaskId(98765L);

        ObjectNode caseData = MAPPER.createObjectNode();
        ArrayNode caseBundles = caseData.putArray("caseBundles");

        ObjectNode bundle = buildCcdBundle();
        bundle.set("stitchedDocument", null);

        caseBundles.add(MAPPER.createObjectNode().set("value", bundle));
        response.setData(caseData);
        return response;
    }

    public static CcdCallbackResponseDto createNewBundleResponse() {
        CcdCallbackResponseDto response = new CcdCallbackResponseDto();
        response.setDocumentTaskId(12345L);

        ObjectNode caseData = MAPPER.createObjectNode();
        caseData.put("caseTitle", "My Test Case");
        ArrayNode caseBundles = caseData.putArray("caseBundles");

        caseBundles.add(MAPPER.createObjectNode().set("value", buildCcdBundle()));
        response.setData(caseData);
        return response;
    }

    public static CcdCallbackResponseDto createCloneBundleResponse() {
        CcdCallbackResponseDto response = new CcdCallbackResponseDto();
        ObjectNode caseData = MAPPER.createObjectNode();
        ArrayNode caseBundles = caseData.putArray("caseBundles");

        ObjectNode originalBundle = buildCcdBundle();
        originalBundle.put("eligibleForCloning", "no");

        ObjectNode clonedBundle = buildCcdBundle();
        clonedBundle.put("id", UUID.randomUUID().toString()); // It gets a new ID
        clonedBundle.put("title", "CLONED_" + originalBundle.get("title").asText());
        clonedBundle.put("fileName", "CLONED_" + originalBundle.get("fileName").asText());
        clonedBundle.put("eligibleForCloning", "no");


        caseBundles.add(MAPPER.createObjectNode().set("value", originalBundle));
        caseBundles.add(MAPPER.createObjectNode().set("value", clonedBundle));

        response.setData(caseData);
        return response;
    }

    private static JsonNode buildCcdDocument() {
        ObjectNode doc = MAPPER.createObjectNode();
        doc.put("document_url", "http://dm-store:8080/documents/b9a3416c-66d4-4a24-9580-a631e78d1275");
        doc.put("document_binary_url", "http://dm-store:8080/documents/b9a3416c-66d4-4a24-9580-a631e78d1275/binary");
        doc.put("document_filename", "stitched.pdf");
        doc.put("document_hash", "sha256-c38944298e827135e533f7c4621d34b4139f408990c6d7a5a894769a6c9d7491");
        return doc;
    }

    private static ObjectNode buildCcdBundle() {
        ObjectNode bundle = MAPPER.createObjectNode();
        bundle.put("id", "a585a03b-a521-443b-826c-9411ebd44733");
        bundle.put("title", "Test Bundle");
        bundle.put("description", "This is a test bundle description.");
        bundle.put("eligibleForStitching", "yes");
        bundle.put("eligibleForCloning", "no");
        bundle.put("fileName", "bundle-filename");
        bundle.put("fileNameIdentifier", "test-identifier");
        bundle.put("coverpageTemplate", "FL-FRM-APP-ENG-00002.docx");
        bundle.put("hasTableOfContents", "Yes");
        bundle.put("hasCoversheets", "Yes");
        bundle.put("hasFolderCoversheets", "No");
        bundle.put("stitchStatus", "DONE");
        bundle.put("paginationStyle", "off");
        bundle.put("pageNumberFormat", "numberOfPages");
        bundle.put("stitchingFailureMessage", "");
        bundle.put("enableEmailNotification", "Yes");

        bundle.set("documents", MAPPER.createArrayNode());
        bundle.set("folders", MAPPER.createArrayNode());
        return bundle;
    }
}