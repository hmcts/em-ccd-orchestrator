package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleDocumentDTO implements Serializable {

    private Long id;

    private String documentId;
    private String name;
    private String description;
    private String documentUri;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDocumentUri() {
        return documentUri;
    }

    public void setDocumentUri(String documentUri) {
        this.documentUri = documentUri;
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
