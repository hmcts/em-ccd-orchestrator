package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.core.JsonParser;
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
import java.util.*;

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
        List<JsonNode> newBundlesList = new ArrayList<>();

        if (maybeBundles.isPresent()) {
            for (int i = 0; i < maybeBundles.get().size(); i++) {
                try {
                    JsonNode originalJson = maybeBundles.get().get(i);

                    CcdBundleDTO originalBundle = this.bundleJsonToBundleDto(originalJson);
                    boolean isEligibleForCloning = originalBundle.getEligibleForCloningAsBoolean();
                    CcdBundleDTO amendedBundle = this.setCloningToFalse(originalBundle);
                    JsonNode amendedJson = this.bundleDtoToBundleJson(amendedBundle);
                    newBundlesList.add(amendedJson);

                    if (isEligibleForCloning) {
                        CcdBundleDTO clonedBundle = this.cloneBundle(amendedBundle);
                        newBundlesList.add(this.bundleDtoToBundleJson(clonedBundle));
                    }
                } catch (IOException e) {
                    return ccdCallbackDto.getCaseData();
                }
            }
            maybeBundles.get().removeAll();
            maybeBundles.get().addAll(newBundlesList);
        }

        return ccdCallbackDto.getCaseData();
    }

    private CcdBundleDTO cloneBundle(CcdBundleDTO originalBundle) {
        CcdBundleDTO clonedBundle = new CcdBundleDTO();

        clonedBundle.setId(originalBundle.getId());
        clonedBundle.setTitle("CLONED " + originalBundle.getTitle());
        clonedBundle.setDescription(originalBundle.getDescription());
        clonedBundle.setEligibleForStitchingAsBoolean(false);
        clonedBundle.setEligibleForCloningAsBoolean(false);
        clonedBundle.setFileName(originalBundle.getFileName());
        clonedBundle.setHasTableOfContents(originalBundle.getHasTableOfContents());
        clonedBundle.setHasCoversheets(originalBundle.getHasCoversheets());

        return clonedBundle;
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

    private CcdBundleDTO setCloningToFalse(CcdBundleDTO bundle) {
        bundle.setEligibleForCloningAsBoolean(false);
        return bundle;
    }

}