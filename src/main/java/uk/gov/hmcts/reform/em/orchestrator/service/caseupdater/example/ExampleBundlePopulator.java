package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ExampleBundlePopulator {

    private final ObjectMapper objectMapper;

    public ExampleBundlePopulator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode populateNewBundle(JsonNode caseData) {
        CcdBundleDTO ccdBundleDTO = new CcdBundleDTO();
        ccdBundleDTO.setTitle("New Bundle");
        List documents = Stream
                .of(
                        new CcdValue(
                                new CcdBundleDocumentDTO(
                                        caseData.at("/caseDocument1Name").asText(),
                                        null,
                                        0,
                                        caseData.at("/caseDocument1/document_binary_url").asText()
                                )
                        )
                )
                .collect(Collectors.toList());

        ccdBundleDTO .setDocuments( documents );
        return objectMapper.valueToTree(new CcdValue<>(ccdBundleDTO));
    }

}
