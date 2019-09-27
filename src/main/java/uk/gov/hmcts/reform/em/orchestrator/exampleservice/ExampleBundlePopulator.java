package uk.gov.hmcts.reform.em.orchestrator.exampleservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.*;

@Service
public class ExampleBundlePopulator {

    private final ObjectMapper objectMapper;

    public ExampleBundlePopulator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    //    The following code navigates through the case JSON to find a document's name and URL.
    //    Customise the following code to align with your CCD definition
    public JsonNode populateNewBundle(JsonNode caseData) {
        CcdBundleDTO ccdBundleDTO = new CcdBundleDTO();
        ccdBundleDTO.setTitle("New Bundle");
        ccdBundleDTO.setFileName("exampleservice-bundle.pdf");
        ccdBundleDTO.setHasCoversheets(CcdBoolean.Yes);
        ccdBundleDTO.setHasTableOfContents(CcdBoolean.Yes);
        ccdBundleDTO.setEligibleForStitchingAsBoolean(false);
        ccdBundleDTO.setEligibleForCloningAsBoolean(false);
        ccdBundleDTO.setPageNumberFormat("numberOfPages");

        ArrayNode caseDocuments = (ArrayNode) caseData.findValue("caseDocuments");

        if (caseDocuments != null) {
            ccdBundleDTO.setEligibleForStitchingAsBoolean(true);

            for (int i = 0; i < caseDocuments.size(); i++) {
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
        }

        return objectMapper.valueToTree(new CcdValue<>(ccdBundleDTO));
    }

}
