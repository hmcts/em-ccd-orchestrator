package uk.gov.hmcts.reform.em.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.BundleDocumentDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BundlePopulator {

    private final ObjectMapper mapper = new ObjectMapper();
    private CasePropertyFinder casePropertyFinder;

    public BundlePopulator(CasePropertyFinder casePropertyFinder) {
        this.casePropertyFinder = casePropertyFinder;
    }

    public JsonNode populateNewBundle(JsonNode caseData, List<List<String>> allPathsToDocumentProperties) {

        BundleDTO newBundle = new BundleDTO();
        newBundle.setBundleTitle("New Bundle");

        ArrayList<BundleDocumentDTO> listOfDocs = new ArrayList<>();

        allPathsToDocumentProperties.forEach(documentPropertiesPath -> {
            BundleDocumentDTO newBundleDocument = prepareNewBundleDocumentDTO(caseData, documentPropertiesPath.get(0), documentPropertiesPath.get(1));
            listOfDocs.add(newBundleDocument);
        });

        newBundle.setDocuments(listOfDocs);
        return mapper.valueToTree(newBundle);
    }


    private BundleDocumentDTO prepareNewBundleDocumentDTO(JsonNode caseData, String pathToDocumentName, String pathToDocumentURI) {

        BundleDocumentDTO newBundleDocumentDTO = new BundleDocumentDTO();
        newBundleDocumentDTO.setDocTitle(getDocumentProperty(caseData, pathToDocumentName));
        newBundleDocumentDTO.setDocumentURI(getDocumentProperty(caseData, pathToDocumentURI));

        return newBundleDocumentDTO;
    }

    private String getDocumentProperty(JsonNode caseData, String pathToDocumentProperty) {
        Optional<JsonNode> documentJsonNode = casePropertyFinder.findCaseProperty(caseData, pathToDocumentProperty);
        if (documentJsonNode.isPresent()) {
            return documentJsonNode.get().textValue();
        } else {
            return null; //Find better way of handling failed doc-path errors
        }

    }

}
