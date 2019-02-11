package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDocumentDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdValue;

import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

        List allDocuments = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(caseDocuments.iterator(), Spliterator.ORDERED),
                false).map( caseDocument ->
                new CcdValue(
                        new CcdBundleDocumentDTO(
                                caseDocument.at("/value/name").asText(),
                                null,
                                0,
                                new CcdDocument(
                                        caseDocument.at("/value/document/document_url").asText(),
                                        caseDocument.at("/value/document/document_filename").asText(),
                                        caseDocument.at("/value/document/document_binary_url").asText()
                                )

                        )
                )
        ).collect(Collectors.toList());

        ccdBundleDTO.setDocuments(allDocuments);
        return objectMapper.valueToTree(new CcdValue<>(ccdBundleDTO));
    }

}
