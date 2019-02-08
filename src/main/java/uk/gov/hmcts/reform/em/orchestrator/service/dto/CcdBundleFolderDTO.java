package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CcdBundleFolderDTO {

    private String name;
    private ArrayList<CcdValue<CcdBundleDocumentDTO>> documents = new ArrayList<>();
    private ArrayList<CcdValue<CcdBundleFolderDTO>> folders = new ArrayList<>();
    private int sortIndex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<CcdValue<CcdBundleDocumentDTO>> getDocuments() {
        return documents;
    }

    public void setDocuments(ArrayList<CcdValue<CcdBundleDocumentDTO>> documents) {
        this.documents = documents;
    }

    public ArrayList<CcdValue<CcdBundleFolderDTO>> getFolders() {
        return folders;
    }

    public void setFolders(ArrayList<CcdValue<CcdBundleFolderDTO>> folders) {
        this.folders = folders;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }
}
