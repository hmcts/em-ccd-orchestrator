package uk.gov.hmcts.reform.em.orchestrator.financialremedyservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.JsonNodesVerifier;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;

@Service
public class FinancialRemedyAddCaseBundleService implements CcdCaseUpdater {

    private final FinancialRemedyBundlePopulator financialRemedyBundlePopulator;

    private final JsonNodesVerifier jsonNodesVerifier;

    private final ObjectMapper objectMapper;

    public FinancialRemedyAddCaseBundleService(FinancialRemedyBundlePopulator populator, JsonNodesVerifier verifier, ObjectMapper mapper) {
        jsonNodesVerifier = verifier;
        objectMapper = mapper;
        financialRemedyBundlePopulator = populator;
    }


    @Override
    public boolean handles(CcdCallbackDto ccdCallbackDto) {
        return jsonNodesVerifier.verify(ccdCallbackDto.getCcdPayload());
    }

    @Override
    public JsonNode updateCase(CcdCallbackDto ccdCallbackDto) {

        ArrayNode bundles = ccdCallbackDto
                .findCaseProperty(ArrayNode.class)
                .orElseGet(() -> {
                    ArrayNode arrayNode = objectMapper.createArrayNode();
                    ((ObjectNode) ccdCallbackDto.getCaseData()).set(ccdCallbackDto.getPropertyName().get(), arrayNode);
                    return arrayNode;
                });

        JsonNode newBundle = financialRemedyBundlePopulator.populateNewBundle(ccdCallbackDto.getCaseData());

        bundles.add(newBundle);

        return ccdCallbackDto.getCaseData();

    }


}
