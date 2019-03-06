package uk.gov.hmcts.reform.em.orchestrator.exampleservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.JsonNodesVerifier;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;

@Service
public class ExampleAddCaseBundleService implements CcdCaseUpdater {

    // Link to your service's bundlePopulator
    private final ExampleBundlePopulator exampleBundlePopulator;

    private final JsonNodesVerifier exampleCaseVerifier;

    private final ObjectMapper objectMapper;

    public ExampleAddCaseBundleService(ExampleBundlePopulator exampleBundlePopulator, JsonNodesVerifier exampleCaseVerifier, ObjectMapper objectMapper) {
        this.exampleCaseVerifier = exampleCaseVerifier;
        this.objectMapper = objectMapper;

        // Change this to your service's bundlePopulator
        this.exampleBundlePopulator = exampleBundlePopulator;

    }

    @Override
    public boolean handles(CcdCallbackDto ccdCallbackDto) {
        return exampleCaseVerifier.verify(ccdCallbackDto.getCcdPayload());
    }

    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {

        ArrayNode bundles = ccdCallbackDto
                .findCaseProperty(ArrayNode.class)
                .orElseGet(() -> {
                    ArrayNode arrayNode = objectMapper.createArrayNode();
                    ((ObjectNode)ccdCallbackDto.getCaseData()).set(ccdCallbackDto.getPropertyName().get(), arrayNode);
                    return arrayNode;
                });

        // Change this to your service's bundlePopulator
        JsonNode newBundle = exampleBundlePopulator.populateNewBundle(ccdCallbackDto.getCaseData());

        bundles.add(newBundle);

        return ccdCallbackDto.getCaseData();

    }



}
