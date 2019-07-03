package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.LinkedList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CcdBundleDTO {

    private Long id;
    private String title;
    private String description;
    private String eligibleForStitching;
    private String eligibleForCloning;
    private CcdDocument stitchedDocument;
    private List<CcdValue<CcdBundleDocumentDTO>> documents = new LinkedList<>();
    private List<CcdValue<CcdBundleFolderDTO>> folders = new LinkedList<>();

    @Size(min = 2, max = 30)
    @Pattern(regexp = "^[-._A-Za-z0-9]*$")
    private String fileName;
    private CcdBoolean hasTableOfContents;
    private CcdBoolean hasCoversheets;
    private String stitchStatus;

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

    public void setEligibleForCloning(String eligibleForCloning) {
        this.eligibleForCloning = eligibleForCloning;
    }

    public String getEligibleForCloning() {
        return eligibleForCloning;
    }

    @JsonIgnore
    public boolean getEligibleForCloningAsBoolean() {
        return eligibleForCloning != null && eligibleForCloning.equalsIgnoreCase("yes");
    }

    @JsonIgnore
    public void setEligibleForCloningAsBoolean(boolean eligibleForCloning) {
        this.eligibleForCloning = eligibleForCloning ? "yes" : "no";
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

    public CcdBoolean getHasTableOfContents() {
        return hasTableOfContents;
    }

    public void setHasTableOfContents(CcdBoolean hasTableOfContents) {
        this.hasTableOfContents = hasTableOfContents;
    }

    public CcdBoolean getHasCoversheets() {
        return hasCoversheets;
    }

    public void setHasCoversheets(CcdBoolean hasCoversheets) {
        this.hasCoversheets = hasCoversheets;
    }

    public String getStitchStatus() {
        return stitchStatus;
    }

    public void setStitchStatus(String stitchStatus) {
        this.stitchStatus = stitchStatus;
    }
}
