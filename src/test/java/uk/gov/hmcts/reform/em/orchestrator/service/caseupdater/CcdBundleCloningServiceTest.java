package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class CcdBundleCloningServiceTest {

    private CcdBundleCloningService ccdBundleCloningService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final File jsonOneEligible = new File(ClassLoader
        .getSystemResource("case-one-eligible.json").getPath());

    private final File jsonOneNotEligible = new File(ClassLoader
        .getSystemResource("case-one-not-eligible.json").getPath());

    private final File jsonOneEligibleOneNot = new File(ClassLoader
        .getSystemResource("case-one-eligible-one-not-eligible.json").getPath());

    private final File jsonTwoEligible = new File(ClassLoader
        .getSystemResource("case-two-eligible.json").getPath());

    private final JavaType type = objectMapper.getTypeFactory()
        .constructParametricType(CcdValue.class, CcdBundleDTO.class);

    private CcdCallbackDto createCallbackDto(JsonNode node) {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setPropertyName(Optional.of("caseBundles"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");
        return ccdCallbackDto;
    }

    private CcdBundleDTO getIthBundleDto(ArrayNode bundlesJson, int i) throws Exception {
        JsonNode ithBundleJson = bundlesJson.get(i);
        CcdValue<CcdBundleDTO> ccdValue = objectMapper.readValue(objectMapper.treeAsTokens(ithBundleJson), type);
        return ccdValue.getValue();
    }

    private void checkIthBundleTitle(ArrayNode list, int i, String title) throws Exception {
        CcdBundleDTO bundle = getIthBundleDto(list, i);
        assertEquals(title, bundle.getTitle());
    }

    @BeforeEach
    void setup() {
        ccdBundleCloningService = new CcdBundleCloningService(objectMapper);
    }

    @Test
    void testNameUpdated() throws Exception {
        JsonNode node = objectMapper.readTree(jsonOneEligible);
        CcdCallbackDto ccdCallbackDto = createCallbackDto(node);

        assertEquals(1, node.get("case_details").get("case_data").get("caseBundles").size());

        ccdBundleCloningService.updateCase(ccdCallbackDto);

        ArrayNode updatedBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");
        CcdBundleDTO updatedFirstBundle = getIthBundleDto(updatedBundles, 0);
        CcdBundleDTO updatedSecondBundle = getIthBundleDto(updatedBundles, 1);

        assertEquals(2, node.get("case_details").get("case_data").get("caseBundles").size());
        assertEquals(updatedFirstBundle.getDescription(), updatedSecondBundle.getDescription());
        checkIthBundleTitle(updatedBundles, 0, "CLONED_First Bundle");
        checkIthBundleTitle(updatedBundles, 1, "First Bundle");
    }

    @Test
    void testCloningSetsToFalse() throws Exception {
        JsonNode node = objectMapper.readTree(jsonOneEligible);
        CcdCallbackDto ccdCallbackDto = createCallbackDto(node);

        ArrayNode originalBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");
        CcdBundleDTO originalFirstBundle = getIthBundleDto(originalBundles, 0);

        assertEquals("yes", originalFirstBundle.getEligibleForCloning());

        ccdBundleCloningService.updateCase(ccdCallbackDto);

        ArrayNode updatedBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");

        CcdBundleDTO updatedFirstBundle = getIthBundleDto(updatedBundles, 0);
        CcdBundleDTO updatedSecondBundle = getIthBundleDto(updatedBundles, 1);

        assertFalse(updatedFirstBundle.getEligibleForCloningAsBoolean());
        assertFalse(updatedSecondBundle.getEligibleForCloningAsBoolean());
    }

    @Test
    void testFalseBundlesNotCloned() throws Exception {
        JsonNode node = objectMapper.readTree(jsonOneNotEligible);
        CcdCallbackDto ccdCallbackDto = createCallbackDto(node);

        ArrayNode originalBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");
        CcdBundleDTO originalBundle = getIthBundleDto(originalBundles, 0);

        assertEquals(1, node.get("case_details").get("case_data").get("caseBundles").size());
        assertEquals("no", originalBundle.getEligibleForCloning());

        ccdBundleCloningService.updateCase(ccdCallbackDto);

        ArrayNode updatedBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");

        assertEquals(1, node.get("case_details").get("case_data").get("caseBundles").size());
        assertEquals(0, StringUtils.countMatches(updatedBundles.toString(), "CLONED"));
    }

    @Test
    void testAllEligibleBundlesCloned() throws Exception {
        JsonNode node = objectMapper.readTree(jsonTwoEligible);
        CcdCallbackDto ccdCallbackDto = createCallbackDto(node);
        ArrayNode originalBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");

        assertEquals(2, node.get("case_details").get("case_data").get("caseBundles").size());
        assertEquals(0, StringUtils.countMatches(originalBundles.toString(), "CLONED"));

        ccdBundleCloningService.updateCase(ccdCallbackDto);

        ArrayNode updatedBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");

        assertEquals(4, node.get("case_details").get("case_data").get("caseBundles").size());
        checkIthBundleTitle(updatedBundles, 0, "CLONED_Second Bundle");
        checkIthBundleTitle(updatedBundles, 1, "Second Bundle");
        checkIthBundleTitle(updatedBundles, 2, "CLONED_First Bundle");
        checkIthBundleTitle(updatedBundles, 3, "First Bundle");
    }

    @Test
    void testOnlyEligibleBundlesCloned() throws Exception {
        JsonNode node = objectMapper.readTree(jsonOneEligibleOneNot);
        CcdCallbackDto ccdCallbackDto = createCallbackDto(node);

        ArrayNode originalBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");

        assertEquals(2, node.get("case_details").get("case_data").get("caseBundles").size());
        assertEquals(0, StringUtils.countMatches(originalBundles.toString(), "CLONED"));

        ccdBundleCloningService.updateCase(ccdCallbackDto);

        ArrayNode updatedBundles = (ArrayNode) node.path("case_details").path("case_data").path("caseBundles");

        assertEquals(3, node.get("case_details").get("case_data").get("caseBundles").size());
        checkIthBundleTitle(updatedBundles, 0, "CLONED_First Bundle");
        checkIthBundleTitle(updatedBundles, 1, "First Bundle");
        checkIthBundleTitle(updatedBundles, 2, "Second Bundle");
    }

    @Test
    void testIfMaybeBundlesIsNotPresent() throws Exception {
        JsonNode node = objectMapper.readTree(jsonOneEligible);
        CcdCallbackDto ccdCallbackDto = createCallbackDto(node);
        assertEquals(1, node.get("case_details").get("case_data").get("caseBundles").size());

        ccdCallbackDto.setPropertyName(Optional.of("non-existent property"));
        ccdBundleCloningService.updateCase(ccdCallbackDto);

        assertEquals(1, node.get("case_details").get("case_data").get("caseBundles").size());
    }

    @Test
    void testUpdateCaseIOExceptionInProcessBundle() throws Exception {
        ObjectMapper localSpyMapper = spy(new ObjectMapper());
        CcdBundleCloningService serviceWithSpy = new CcdBundleCloningService(localSpyMapper);

        JsonNode node = objectMapper.readTree(jsonOneEligible);
        CcdCallbackDto ccdCallbackDto = createCallbackDto(node);

        JavaType typeUsed = localSpyMapper.getTypeFactory().constructParametricType(CcdValue.class, CcdBundleDTO.class);

        doThrow(new IOException("Test IOException during bundle processing"))
            .when(localSpyMapper)
            .readValue(any(JsonParser.class), eq(typeUsed));

        JsonNode resultCaseData = serviceWithSpy.updateCase(ccdCallbackDto);
        ArrayNode updatedBundles = (ArrayNode) resultCaseData.path("case_details").path("case_data").path("caseBundles");

        assertEquals(0, updatedBundles.size());
    }

    @Test
    void testUpdateCaseIOExceptionInReorderBundles() throws Exception {
        ObjectMapper localSpyMapper = spy(new ObjectMapper());
        CcdBundleCloningService serviceWithSpy = new CcdBundleCloningService(localSpyMapper);

        JsonNode node = objectMapper.readTree(jsonOneEligible);
        CcdCallbackDto ccdCallbackDto = createCallbackDto(node);

        JavaType typeUsed = localSpyMapper.getTypeFactory().constructParametricType(CcdValue.class, CcdBundleDTO.class);

        doAnswer(new Answer<CcdValue<CcdBundleDTO>>() {
            private int count = 0;
            @Override
            public CcdValue<CcdBundleDTO> answer(InvocationOnMock invocation) throws Throwable {
                count++;
                if (count == 3) {
                    throw new IOException("Test IOException during bundle reordering");
                }
                return (CcdValue<CcdBundleDTO>) invocation.callRealMethod();
            }
        }).when(localSpyMapper).readValue(any(JsonParser.class), eq(typeUsed));

        JsonNode resultCaseData = serviceWithSpy.updateCase(ccdCallbackDto);
        ArrayNode updatedBundles = (ArrayNode) resultCaseData.path("case_details").path("case_data").path("caseBundles");

        assertEquals(2, updatedBundles.size());

        CcdBundleDTO bundle1 = getIthBundleDto(updatedBundles, 0);
        CcdBundleDTO bundle2 = getIthBundleDto(updatedBundles, 1);

        assertEquals("First Bundle", bundle1.getTitle());
        assertTrue(bundle1.getEligibleForCloningAsBoolean());

        assertEquals("CLONED_First Bundle", bundle2.getTitle());
        assertTrue(bundle2.getEligibleForCloningAsBoolean());
    }
}