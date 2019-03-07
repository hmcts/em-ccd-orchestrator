package uk.gov.hmcts.reform.em.orchestrator.financialremedyservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;


@Service
public class FinancialRemedyBundlePopulator {

    private final ObjectMapper objectMapper;

    public FinancialRemedyBundlePopulator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JsonNode populateNewBundle(JsonNode caseData) {
        CcdBundleDTO ccdBundleDTO = new CcdBundleDTO();
        ccdBundleDTO.setTitle("New Bundle");

        JsonNode scanned4AFormDocument = caseData.findValue("uploadScanned4AForm");
        JsonNode scanned4BFormDocument = caseData.findValue("uploadScanned4BForm");
        JsonNode miniFormADocument = caseData.findValue("miniFormA");

        JsonNode additionalDocuments = caseData.findValue("uploadAdditionalDocument");

        addDocumentToBundle(ccdBundleDTO, scanned4AFormDocument, "scanned4AFormDocument", 0);
        addDocumentToBundle(ccdBundleDTO, scanned4BFormDocument, "scanned4BFormDocument", 1);
        addDocumentToBundle(ccdBundleDTO, miniFormADocument, "miniFormADocument", 2);

        if (additionalDocuments != null) {
            ccdBundleDTO.setEligibleForStitchingAsBoolean(true);

            for (int i = 0; i < additionalDocuments.size(); i++) {
                JsonNode caseDocument = additionalDocuments.get(i);
                ccdBundleDTO.getDocuments().add(
                        new CcdValue(
                                new CcdBundleDocumentDTO(
                                        "additionalDocument" + i,
                                        null,
                                        i + 3,
                                        new CcdDocument(
                                                caseDocument.at("/value/additionalDocuments/document_url").asText(),
                                                caseDocument.at("/value/additionalDocuments/document_filename").asText(),
                                                caseDocument.at("/value/additionalDocuments/document_binary_url").asText()
                                        )

                                )
                        )
                );
            }
        }
        return objectMapper.valueToTree(new CcdValue<>(ccdBundleDTO));
    }

    private static void addDocumentToBundle(CcdBundleDTO ccdBundleDTO, JsonNode jsonNode, String name, int index) {
        if (jsonNode != null) {
            ccdBundleDTO.setEligibleForStitchingAsBoolean(true);
            ccdBundleDTO.getDocuments().add(new CcdValue(
                    new CcdBundleDocumentDTO("scanned4AFormDocument",
                            null,
                            index,
                            new CcdDocument(
                                    jsonNode.at("/document_url").asText(),
                                    jsonNode.at("/document_filename").asText(),
                                    jsonNode.at("/document_binary_url").asText()
                            )

                    )
            ));
        }
    }

}
