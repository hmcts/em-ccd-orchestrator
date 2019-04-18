package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.LinkedList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CcdBundleDTO {

    private Long id;
    private String title;
    private String description;
    private String eligibleForStitching;
    private CcdDocument stitchedDocument;
    private List<CcdValue<CcdBundleDocumentDTO>> documents = new LinkedList<>();
    @JsonIgnore
    private List<CcdValue<CcdBundleFolderDTO>> folders = new LinkedList<>();
    private String fileName;
    private boolean hasTableOfContents = true;
    private boolean hasCoversheets = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String isEligibleForStitching() {
        return eligibleForStitching;
    }

    public void setEligibleForStitching(String eligibleForStitching) {
        this.eligibleForStitching = eligibleForStitching;
    }

    public String getEligibleForStitching() {
        return eligibleForStitching;
    }

    @JsonIgnore
    public boolean getEligibleForStitchingAsBoolean() {
        return eligibleForStitching != null && eligibleForStitching.equalsIgnoreCase("yes");
    }

    @JsonIgnore
    public void setEligibleForStitchingAsBoolean(boolean eligibleForStitching) {
        this.eligibleForStitching = eligibleForStitching ? "yes" : "no";
    }

    public CcdDocument getStitchedDocument() {
        return stitchedDocument;
    }

    public void setStitchedDocument(CcdDocument stitchedDocument) {
        this.stitchedDocument = stitchedDocument;
    }

    public List<CcdValue<CcdBundleDocumentDTO>> getDocuments() {
        return documents;
    }

    public void setDocuments(List<CcdValue<CcdBundleDocumentDTO>> documents) {
        this.documents = documents;
    }

    public List<CcdValue<CcdBundleFolderDTO>> getFolders() {
        return folders;
    }

    public void setFolders(List<CcdValue<CcdBundleFolderDTO>> folders) {
        this.folders = folders;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean hasTableOfContents() {
        return hasTableOfContents;
    }

    public void setHasTableOfContents(boolean hasTableOfContents) {
        this.hasTableOfContents = hasTableOfContents;
    }

    public boolean hasCoversheets() {
        return hasCoversheets;
    }

    public void setHasCoversheets(boolean hasCoversheets) {
        this.hasCoversheets = hasCoversheets;
    }
}
