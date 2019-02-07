package uk.gov.hmcts.reform.em.orchestrator.stitching.dto;

import java.util.ArrayList;
import java.util.List;

public class StitchingBundleFolderDTO {

    private String description;
    private String folderName;
    private List<StitchingBundleDocumentDTO> documents = new ArrayList<>();
    private List<StitchingBundleFolderDTO> folders = new ArrayList<>();
    private int sortIndex;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public List<StitchingBundleDocumentDTO> getDocuments() {
        return documents;
    }

    public void setDocuments(List<StitchingBundleDocumentDTO> documents) {
        this.documents = documents;
    }

    public List<StitchingBundleFolderDTO> getFolders() {
        return folders;
    }

    public void setFolders(List<StitchingBundleFolderDTO> folders) {
        this.folders = folders;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

}
