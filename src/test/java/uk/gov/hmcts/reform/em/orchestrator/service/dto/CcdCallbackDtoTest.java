package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CcdCallbackDtoTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final File jsonFile = new File(ClassLoader.getSystemResource("case.json").getPath());
    private final JavaType type = mapper.getTypeFactory().constructParametricType(CcdValue.class, BundleDTO.class);

    @Test
    public void testDeserialization() throws IOException {
        JsonNode root = mapper.readTree(jsonFile);
        ArrayNode bundles = (ArrayNode) root.path("case_details").path("case_data").path("caseBundles");
        JsonNode firstBundle = bundles.get(0);

        CcdValue<BundleDTO> bundleDTO = mapper.readValue(mapper.treeAsTokens(firstBundle), type);

        assertEquals("Bundle Title", bundleDTO.getValue().getBundleTitle());
    }
}