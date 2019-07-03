package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;
import static pl.touk.throwing.ThrowingFunction.unchecked;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CcdBundleCloningService {

    private final ObjectMapper objectMapper;
    private final JavaType type;

    public CcdBundleCloningService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        type = objectMapper.getTypeFactory().constructParametricType(CcdValue.class, CcdBundleDTO.class);
    }

    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {

        Optional<ArrayNode> maybeBundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);

        maybeBundles = maybeBundles.map(bundles -> {
            ArrayNode processedBundlesList = objectMapper.createArrayNode();
            try {
                for (JsonNode bundleJson : bundles) {
                    List<JsonNode> processedBundleOrBundles = processBundle(bundleJson);
                    processedBundlesList.addAll(processedBundleOrBundles);
                }
                return processedBundlesList;
            } catch (IOException e) {
                return bundles;
            }
        });

        return ccdCallbackDto.getCaseData();
    }

    private List<JsonNode> processBundle(JsonNode originalJson) throws IOException {
        List<JsonNode> returnList = new ArrayList<>();

        CcdBundleDTO originalBundle = bundleJsonToBundleDto(originalJson);
        if (originalBundle.getEligibleForCloningAsBoolean()) {
            originalBundle.setEligibleForCloningAsBoolean(false);

            JsonNode originalProcessedJson = bundleDtoToBundleJson(originalBundle);
            JsonNode clonedJson = cloneBundle(originalJson);

            returnList.add(originalProcessedJson);
            returnList.add(clonedJson);
        } else {
            returnList.add(originalJson);
        }

        return returnList;
    }

    private JsonNode cloneBundle(JsonNode originalJson) throws IOException {
        JsonNode unprocessedClonedJson = originalJson.deepCopy();
        CcdBundleDTO clonedBundle = bundleJsonToBundleDto(unprocessedClonedJson);
        clonedBundle.setTitle(clonedBundle.getTitle() + " - CLONED");
        clonedBundle.setFileName(clonedBundle.getFileName() + " - CLONED");
        return bundleDtoToBundleJson(clonedBundle);
    }

    private CcdBundleDTO bundleJsonToBundleDto(JsonNode jsonNode) throws IOException {
        CcdValue<CcdBundleDTO> ccdValue = objectMapper.readValue(objectMapper.treeAsTokens(jsonNode), type);
        return ccdValue.getValue();
    }

    private JsonNode bundleDtoToBundleJson(CcdBundleDTO ccdBundle) {
        CcdValue<CcdBundleDTO> ccdValue = new CcdValue<>();
        ccdValue.setValue(ccdBundle);
        return objectMapper.convertValue(ccdValue, JsonNode.class);
    }

}
