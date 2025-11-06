package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class CcdBundleDocumentDTO implements Serializable {

    private String name;
    private String description;
    private int sortIndex;
    private CcdDocument sourceDocument;

    public CcdBundleDocumentDTO() {

    }

    public CcdBundleDocumentDTO(String name, String description, int sortIndex, CcdDocument sourceDocument) {
        this.name = name;
        this.description = description;
        this.sortIndex = sortIndex;
        this.sourceDocument = sourceDocument;
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

    public CcdDocument getSourceDocument() {
        return sourceDocument;
    }

    public void setSourceDocument(CcdDocument sourceDocument) {
        this.sourceDocument = sourceDocument;
    }
}
