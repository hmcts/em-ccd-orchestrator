package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CcdBundleDocumentDTO {

    private String name;
    private String description;
    private int sortIndex;
    private String documentUri;

    public CcdBundleDocumentDTO() {

    }

    public CcdBundleDocumentDTO(String name, String description, int sortIndex, String documentUri) {
        this.name = name;
        this.description = description;
        this.sortIndex = sortIndex;
        this.documentUri = documentUri;
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

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public String getDocumentUri() {
        return documentUri;
    }

    public void setDocumentUri(String documentUri) {
        this.documentUri = documentUri;
    }
}
