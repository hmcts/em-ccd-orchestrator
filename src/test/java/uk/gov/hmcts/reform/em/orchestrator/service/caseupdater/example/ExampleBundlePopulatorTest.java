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
                .populateNewBundle(objectMapper.readTree("{\"case_details\": {\"case_data\":{\"caseDocument1Name\":\"x\"}}}"));
        assertEquals("New Bundle", jsonNode.at("/bundleTitle").asText());
        assertEquals("x", jsonNode.at("/documents").get(0).at("/value/docTitle").asText());
    }
}