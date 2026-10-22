package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


class CcdBundleDtoTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final File jsonFile = new File(ClassLoader.getSystemResource("case.json").getPath());
    private final JavaType type = mapper.getTypeFactory().constructParametricType(CcdValue.class, CcdBundleDTO.class);

    @Test
    void testDeserialization() throws IOException {
        JsonNode root = mapper.readTree(jsonFile);
        ArrayNode bundles = (ArrayNode) root.path("case_details").path("case_data").path("caseBundles");
        JsonNode firstBundle = bundles.get(0);

        CcdValue<CcdBundleDTO> bundleDTO = mapper.readValue(mapper.treeAsTokens(firstBundle), type);

        assertEquals("Bundle Title", bundleDTO.getValue().getTitle());
    }
}
