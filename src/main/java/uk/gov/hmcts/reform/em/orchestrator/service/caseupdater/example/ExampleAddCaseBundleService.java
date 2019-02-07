package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.JsonNodesVerifier;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.PropertyNotFoundException;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;

@Service
public class ExampleAddCaseBundleService implements CcdCaseUpdater {

    private final ExampleBundlePopulator exampleBundlePopulator;

    private final JsonNodesVerifier exampleCaseVerifier;

    public ExampleAddCaseBundleService(ExampleBundlePopulator exampleBundlePopulator, JsonNodesVerifier exampleCaseVerifier) {
        this.exampleBundlePopulator = exampleBundlePopulator;
        this.exampleCaseVerifier = exampleCaseVerifier;
    }

    @Override
    public boolean handles(CcdCallbackDto ccdCallbackDto) {
        return exampleCaseVerifier.verify(ccdCallbackDto.getCaseData());
    }

    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {

        return ccdCallbackDto
                .findCaseProperty(ArrayNode.class)
                .map( bundleArray -> {
                    JsonNode newBundle = exampleBundlePopulator.populateNewBundle(ccdCallbackDto.getCaseData());
                    bundleArray.add(newBundle);
                    return ccdCallbackDto.getCaseData();
                })
                .orElseThrow(() -> new PropertyNotFoundException(ccdCallbackDto.getPropertyName().get()));

    }

}
