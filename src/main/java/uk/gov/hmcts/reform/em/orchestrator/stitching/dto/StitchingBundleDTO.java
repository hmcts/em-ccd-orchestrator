package uk.gov.hmcts.reform.em.orchestrator.stitching.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StitchingBundleDTO {

    private String bundleTitle;
    private String description;
    private String stitchedDocumentURI;
    private List<StitchingBundleFolderDTO> folders = new ArrayList<>();
    private List<StitchingBundleDocumentDTO> documents = new ArrayList<>();
    private String fileName;
    private String coverpageTemplate;
    private boolean hasTableOfContents;
    private boolean hasCoversheets;
    private boolean hasFolderCoversheets;

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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCoverpageTemplate() {
        return coverpageTemplate;
    }

    public void setCoverpageTemplate(String coverpageTemplate) {
        this.coverpageTemplate = coverpageTemplate;
    }

    public boolean getHasTableOfContents() {
        return hasTableOfContents;
    }

    public void setHasTableOfContents(boolean hasTableOfContents) {
        this.hasTableOfContents = hasTableOfContents;
    }

    public boolean getHasCoversheets() {
        return hasCoversheets;
    }

    public void setHasCoversheets(boolean hasCoversheets) {
        this.hasCoversheets = hasCoversheets;
    }

    public boolean getHasFolderCoversheets() {
        return hasFolderCoversheets;
    }

    public void setHasFolderCoversheets(boolean hasFolderCoversheets) {
        this.hasFolderCoversheets = hasFolderCoversheets;
    }

    public String getStitchedDocumentURI() {
        return stitchedDocumentURI;
    }

    public void setStitchedDocumentURI(String stitchedDocumentURI) {
        this.stitchedDocumentURI = stitchedDocumentURI;
    }
}

