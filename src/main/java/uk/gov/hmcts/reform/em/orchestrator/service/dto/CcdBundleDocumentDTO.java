package uk.gov.hmcts.reform.em.orchestrator.service.dto;

public class CcdBundleDocumentDTO {

    private String name;
    private String description;
    private Integer sortIndex;
    private String documentUri;

    public CcdBundleDocumentDTO(String name, String description, Integer sortIndex, String documentUri) {
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

    public Integer getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(Integer sortIndex) {
        this.sortIndex = sortIndex;
    }

    public String getDocumentUri() {
        return documentUri;
    }

    public void setDocumentUri(String documentUri) {
        this.documentUri = documentUri;
    }
}
