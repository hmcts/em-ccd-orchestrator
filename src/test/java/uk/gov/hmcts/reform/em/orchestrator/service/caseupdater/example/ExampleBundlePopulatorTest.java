package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExampleBundlePopulatorTest {

    ObjectMapper objectMapper = new ObjectMapper();

    ExampleBundlePopulator exampleBundlePopulator = new ExampleBundlePopulator(objectMapper);

    @Test
    public void populateNewBundle() throws Exception {
        JsonNode jsonNode = exampleBundlePopulator
                .populateNewBundle(objectMapper.readTree("{\"caseDocuments\":"
                        + "[ { \"value\": {\"name\":\"namex\", \"document\": {\"document_url\": \"doc_url\", "
                        + "\"document_filename\":\"doc_fn\",\"document_binary_url\":\"doc_b_url\"}}}]}"));
        assertEquals("New Bundle", jsonNode.at("/value/title").asText());
        assertEquals("namex", jsonNode.at("/value/documents").get(0).at("/value/name").asText());
        assertEquals("doc_url",
                jsonNode.at("/value/documents").get(0).at("/value/sourceDocument/document_url").asText());
        assertEquals("doc_fn",
                jsonNode.at("/value/documents").get(0).at("/value/sourceDocument/document_filename").asText());
        assertEquals("doc_b_url",
                jsonNode.at("/value/documents").get(0).at("/value/sourceDocument/document_binary_url").asText());
    }
}
