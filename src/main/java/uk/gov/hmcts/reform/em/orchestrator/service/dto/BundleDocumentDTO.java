package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleDocumentDTO implements Serializable {

    private Long id;

    private String documentId;
    private String docTitle;
    private String docDescription;
    private String documentURI;
    private Instant dateAddedToCase;
    private boolean isIncludedInBundle;
    private String creatorRole;
    private int sortIndex;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    public String getDocDescription() {
        return docDescription;
    }

    public void setDocDescription(String docDescription) {
        this.docDescription = docDescription;
    }

    public String getDocumentURI() {
        return documentURI;
    }

    public void setDocumentURI(String documentURI) {
        this.documentURI = documentURI;
    }

    public Instant getDateAddedToCase() {
        return dateAddedToCase;
    }

    public void setDateAddedToCase(Instant dateAddedToCase) {
        this.dateAddedToCase = dateAddedToCase;
    }

    public boolean isIncludedInBundle() {
        return isIncludedInBundle;
    }

    public void setIncludedInBundle(boolean includedInBundle) {
        isIncludedInBundle = includedInBundle;
    }

    public String getCreatorRole() {
        return creatorRole;
    }

    public void setCreatorRole(String creatorRole) {
        this.creatorRole = creatorRole;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }
}
