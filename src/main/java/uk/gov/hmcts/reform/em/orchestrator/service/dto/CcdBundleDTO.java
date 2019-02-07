package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import java.util.List;

public class CcdBundleDTO {

    private Long id;
    private String title;
    private String description;
    private boolean eligibleForStitching;
    private String stitchStatus;
    private String stitchedDocumentURI;
    private List<CcdValue<CcdBundleDocumentDTO>> documents;

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

    public boolean isEligibleForStitching() {
        return eligibleForStitching;
    }

    public void setEligibleForStitching(boolean eligibleForStitching) {
        this.eligibleForStitching = eligibleForStitching;
    }

    public String getStitchStatus() {
        return stitchStatus;
    }

    public void setStitchStatus(String stitchStatus) {
        this.stitchStatus = stitchStatus;
    }

    public String getStitchedDocumentURI() {
        return stitchedDocumentURI;
    }

    public void setStitchedDocumentURI(String stitchedDocumentURI) {
        this.stitchedDocumentURI = stitchedDocumentURI;
    }

    public List<CcdValue<CcdBundleDocumentDTO>> getDocuments() {
        return documents;
    }

    public void setDocuments(List<CcdValue<CcdBundleDocumentDTO>> documents) {
        this.documents = documents;
    }
}
