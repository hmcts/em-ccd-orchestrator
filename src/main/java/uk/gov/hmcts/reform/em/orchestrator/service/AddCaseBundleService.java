package uk.gov.hmcts.reform.em.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.impl.IncorrectCcdCaseBundlesException;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddCaseBundleService implements CcdCaseUpdater {

    private final BundlePopulator bundlePopulator;

    public AddCaseBundleService(BundlePopulator bundlePopulator) {
        this.bundlePopulator = bundlePopulator;
    }

    @Override
    public void updateCase(JsonNode caseData, JsonNode bundleData, String jwt) {

        ArrayNode bundleArray = castJsonDataToJsonArray(bundleData);

        JsonNode newBundle = bundlePopulator.populateNewBundle(caseData);

        bundleArray.add(newBundle);

    }

    private ArrayNode castJsonDataToJsonArray(JsonNode bundleData) {
        try {
            return (ArrayNode) bundleData;
        } catch (ClassCastException e) {
            throw new IncorrectCcdCaseBundlesException( "Bundle data is not in correct format", e);
        }
    }


}
