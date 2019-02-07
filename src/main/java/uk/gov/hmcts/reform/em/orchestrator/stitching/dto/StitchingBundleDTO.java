package uk.gov.hmcts.reform.em.orchestrator.stitching.dto;

import java.util.ArrayList;
import java.util.List;

public class StitchingBundleDTO {

    private String bundleTitle;
    private String description;
    private List<StitchingBundleFolderDTO> folders = new ArrayList<>();
    private List<StitchingBundleDocumentDTO> documents = new ArrayList<>();

    public String getBundleTitle() {
        return bundleTitle;
    }

    public void setBundleTitle(String bundleTitle) {
        this.bundleTitle = bundleTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<StitchingBundleFolderDTO> getFolders() {
        return folders;
    }

    public void setFolders(List<StitchingBundleFolderDTO> folders) {
        this.folders = folders;
    }

    public List<StitchingBundleDocumentDTO> getDocuments() {
        return documents;
    }

    public void setDocuments(List<StitchingBundleDocumentDTO> documents) {
        this.documents = documents;
    }

}

