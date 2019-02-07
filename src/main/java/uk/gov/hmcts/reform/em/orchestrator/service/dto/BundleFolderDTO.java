package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleFolderDTO implements Serializable {

    @JsonIgnore
    private Long id;

    private String description;
    private String folderName;
    private List<CcdValue<BundleDocumentDTO>> documents = new ArrayList<>();
    private List<CcdValue<BundleFolderDTO>> folders = new ArrayList<>();
    private int sortIndex;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public List<CcdValue<BundleDocumentDTO>> getDocuments() {
        return documents;
    }

    public void setDocuments(List<CcdValue<BundleDocumentDTO>> documents) {
        this.documents = documents;
    }

    public List<CcdValue<BundleFolderDTO>> getFolders() {
        return folders;
    }

    public void setFolders(List<CcdValue<BundleFolderDTO>> folders) {
        this.folders = folders;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }
}

