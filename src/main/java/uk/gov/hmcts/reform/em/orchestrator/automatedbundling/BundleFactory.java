package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.*;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Creates a new bundle from a bundle configuration and some case json.
 */
public class BundleFactory {

    public CcdBundleDTO create(BundleConfiguration configuration, JsonNode caseJson) throws DocumentSelectorException {
        CcdBundleDTO bundle = new CcdBundleDTO();
        bundle.setTitle(configuration.title);
        bundle.setHasCoversheetsAsBoolean(configuration.hasCoversheets);
        bundle.setHasTableOfContentsAsBoolean(configuration.hasTableOfContents);
        bundle.setHasFolderCoversheetsAsBoolean(configuration.hasFolderCoversheets);
        bundle.setFileName(configuration.filename);
        bundle.setEligibleForCloningAsBoolean(false);
        bundle.setEligibleForStitchingAsBoolean(false);

        addFolders(configuration.folders, bundle.getFolders(), caseJson);
        addDocuments(configuration.documents, bundle.getDocuments(), caseJson);

        return bundle;
    }

    private void addFolders(List<BundleConfigurationFolder> sourceFolders,
                            List<CcdValue<CcdBundleFolderDTO>> destinationFolders,
                            JsonNode caseData) throws DocumentSelectorException {
        int sortIndex = 0;

        for (BundleConfigurationFolder folder : sourceFolders) {
            CcdBundleFolderDTO ccdFolder = new CcdBundleFolderDTO();
            ccdFolder.setName(folder.name);
            ccdFolder.setSortIndex(sortIndex++);
            destinationFolders.add(new CcdValue<>(ccdFolder));

            addDocuments(folder.documents, ccdFolder.getDocuments(), caseData);

            if (folder.folders != null && !folder.folders.isEmpty()) {
                addFolders(folder.folders, ccdFolder.getFolders(), caseData);
            }
        }
    }

    private void addDocuments(List<BundleConfigurationDocumentSelector> sourceDocuments,
                              List<CcdValue<CcdBundleDocumentDTO>> destinationDocuments,
                              JsonNode caseData) throws DocumentSelectorException {

        for (BundleConfigurationDocumentSelector selector : sourceDocuments) {
            List<CcdValue<CcdBundleDocumentDTO>> documents = selector instanceof BundleConfigurationDocument
                ? addDocument((BundleConfigurationDocument) selector, caseData)
                : addDocumentSet((BundleConfigurationDocumentSet) selector, caseData);

            destinationDocuments.addAll(documents);
        }
    }

    private List<CcdValue<CcdBundleDocumentDTO>> addDocument(BundleConfigurationDocument documentSelector,
                                                             JsonNode caseData) throws DocumentSelectorException {
        ArrayList<CcdValue<CcdBundleDocumentDTO>> list = new ArrayList<>();
        JsonNode node = caseData.at(documentSelector.property);

        if (node.isMissingNode()) {
            throw new DocumentSelectorException("Could not find element: " + documentSelector.property);
        }

        if (!node.isArray()) {
            throw new DocumentSelectorException("Element is an array: " + documentSelector.property);
        }

        list.add(getDocumentFromNode(node));

        return list;
    }

    private CcdValue<CcdBundleDocumentDTO> getDocumentFromNode(JsonNode node) throws DocumentSelectorException {
        CcdDocument sourceDocument = new CcdDocument();

        sourceDocument.setUrl(getField(node, "/documentLink/document_url").asText());
        sourceDocument.setBinaryUrl(getField(node, "/documentLink/document_binary_url").asText());
        sourceDocument.setFileName(getField(node, "/documentLink/document_filename").asText());

        CcdBundleDocumentDTO document = new CcdBundleDocumentDTO();
        document.setName(getField(node, "/documentName").asText());
        document.setSourceDocument(sourceDocument);

        return new CcdValue<>(document);
    }

    private JsonNode getField(JsonNode outerNode, String path) throws DocumentSelectorException {
        JsonNode innerNode = outerNode.at(path);
        if (innerNode.isMissingNode()) {
            throw new DocumentSelectorException("Could not find the property " + path + " in the node: " + outerNode.asText());
        }
        return innerNode;
    }

    private List<CcdValue<CcdBundleDocumentDTO>> addDocumentSet(BundleConfigurationDocumentSet documentSelector,
                                                                JsonNode caseData) throws DocumentSelectorException {

        JsonNode list = caseData.at(documentSelector.property);

        if (list.isMissingNode()) {
            throw new DocumentSelectorException("Could not find element: " + documentSelector.property);
        }

        if (!list.isArray()) {
            throw new DocumentSelectorException("Element is not an array: " + documentSelector.property);
        }

        return StreamSupport
            .stream(list.spliterator(), true)
            .map(n -> n.at("/value"))
            .filter(n -> anyFilterMatches(documentSelector.filters, n))
            .map(this::getDocumentFromNode) //TODO Deal with this exception in the map
            .collect(Collectors.toList());
    }

    private boolean anyFilterMatches(List<BundleConfigurationDocumentSet.BundleConfigurationFilter> filters,
                                     JsonNode node) {

        for (BundleConfigurationDocumentSet.BundleConfigurationFilter filter : filters) {
            if (!node.at(filter.property).asText().equals(filter.value)) {
                return false;
            }
        }

        return true;
    }
}
