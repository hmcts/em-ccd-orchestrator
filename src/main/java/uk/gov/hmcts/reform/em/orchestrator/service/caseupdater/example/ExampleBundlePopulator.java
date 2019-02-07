package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDocumentDTO;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExampleBundlePopulator {

    private final ObjectMapper objectMapper;

    public ExampleBundlePopulator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode populateNewBundle(JsonNode caseData) {
        BundleDTO newBundle = new BundleDTO();
        newBundle.setBundleTitle("New Bundle");
        BundleDocumentDTO bundleDocumentDTO = new BundleDocumentDTO();
        bundleDocumentDTO.setDocTitle(caseData.at("/case_details/case_data/caseDocument1Name").asText());
        bundleDocumentDTO.setDocumentURI(caseData.at("/case_details/case_data/caseDocument1").asText());
        newBundle.setDocuments(Stream.of(bundleDocumentDTO).collect(Collectors.toList()));
        return objectMapper.valueToTree(newBundle);
    }

}
