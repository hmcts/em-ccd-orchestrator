package uk.gov.hmcts.reform.em.orchestrator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import uk.gov.hmcts.reform.em.orchestrator.service.impl.IncorrectCcdCaseBundlesException;

import java.util.*;

public class addCaseBundleService implements CcdCaseUpdater {

    private final ObjectMapper mapper = new ObjectMapper();
    private final BundlePopulator bundlePopulator= new BundlePopulator();


    // TODO link to service-specific ArrayList - need to pass a service-specific list like this from updateCase.
    private List<List<String>> examplePaths() {
        List<String> examplePath = new ArrayList<>();
        examplePath.add("path.to.document.name");
        examplePath.add("path.to.document.uri");

        List<List<String>> examplePaths = new ArrayList<>();
        examplePaths.add(examplePath);
        examplePaths.add(examplePath);
        return examplePaths;
    }


    @Override
    public void updateCase(JsonNode caseData, JsonNode bundleData, String jwt) {

        ArrayNode bundleArray = castJsonDataToJsonArray(bundleData);
        JsonNode newBundle = bundlePopulator.populateNewBundle(caseData, examplePaths());

        bundleArray.add(newBundle);

        // replace existing caseData's caseBundles with new caseBundles? Or is casting enough?
    }

//    private BundleDTO bundleJsonToBundleDto(JsonNode jsonNode) throws JsonProcessingException {
//        return mapper.treeToValue(jsonNode, BundleDTO.class);
//    }
//

    private ArrayNode castJsonDataToJsonArray(JsonNode bundleData) {
        try {
            return (ArrayNode) bundleData;
        } catch (ClassCastException e) {
            throw new IncorrectCcdCaseBundlesException( "Bundle data is not in correct format", e);
        }
    }


}
