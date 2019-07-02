package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CcdBundleCloningService implements CcdCaseUpdater {

    private final ObjectMapper objectMapper;
    private final JavaType type;

    public CcdBundleCloningService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        type = objectMapper.getTypeFactory().constructParametricType(CcdValue.class, CcdBundleDTO.class);
    }

    @Override
    public boolean handles(CcdCallbackDto ccdCallbackDto) {
        return false;
    }

    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {

        Optional<ArrayNode> maybeBundles = ccdCallbackDto.findCaseProperty(ArrayNode.class);
        List<JsonNode> updatedBundlesList = new ArrayList<>();

        if (maybeBundles.isPresent()) {
            for (int i = 0; i < maybeBundles.get().size(); i++) {
                try {
                    JsonNode originalJson = maybeBundles.get().get(i);

                    CcdBundleDTO originalBundle = bundleJsonToBundleDto(originalJson);
                    boolean isEligibleForCloning = originalBundle.getEligibleForCloningAsBoolean();
                    if (isEligibleForCloning) {
                        originalBundle.setEligibleForCloningAsBoolean(false);
                    }
                    JsonNode processedOriginalJson = bundleDtoToBundleJson(originalBundle);
                    updatedBundlesList.add(processedOriginalJson);

                    if (isEligibleForCloning) {
                        JsonNode initialClonedJson = processedOriginalJson.deepCopy();

                        CcdBundleDTO unprocessedClonedBundle = bundleJsonToBundleDto(initialClonedJson);
                        unprocessedClonedBundle.setTitle(originalBundle.getTitle() + " - CLONED");
                        unprocessedClonedBundle.setFileName(originalBundle.getFileName() + " - CLONED");
                        JsonNode inProgressClonedJson = bundleDtoToBundleJson(unprocessedClonedBundle);
                        updatedBundlesList.add(inProgressClonedJson);
                    }
                } catch (IOException e) {
                    return ccdCallbackDto.getCaseData();
                }
            }
            maybeBundles.get().removeAll();
            maybeBundles.get().addAll(updatedBundlesList);

        }
        return ccdCallbackDto.getCaseData();
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
