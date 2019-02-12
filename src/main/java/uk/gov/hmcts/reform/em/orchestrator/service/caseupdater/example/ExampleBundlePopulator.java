package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

@Service
public class ExampleBundlePopulator {

    private final ObjectMapper objectMapper;

    public ExampleBundlePopulator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode populateNewBundle(JsonNode caseData) {
        CcdBundleDTO ccdBundleDTO = new CcdBundleDTO();
        ccdBundleDTO.setTitle("New Bundle");

        ArrayNode caseDocuments = (ArrayNode) caseData.findValue("caseDocuments");
        ccdBundleDTO.setEligibleForStitchingAsBoolean(true);

        for (int i = 0; i<caseDocuments.size(); i++) {
            JsonNode caseDocument = caseDocuments.get(i);
            ccdBundleDTO.getDocuments().add(
                new CcdValue(
                    new CcdBundleDocumentDTO(
                        caseDocument.at("/value/name").asText(),
                        null,
                        i,
                        new CcdDocument(
                                caseDocument.at("/value/document/document_url").asText(),
                                caseDocument.at("/value/document/document_filename").asText(),
                                caseDocument.at("/value/document/document_binary_url").asText()
                        )

                    )
                )
            );
        }

        return objectMapper.valueToTree(new CcdValue<>(ccdBundleDTO));
    }

}
