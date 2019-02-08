package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CcdDocument {

    @JsonProperty("document_url")
    private String url;
    @JsonProperty("document_filename")
    private String fileName;
    @JsonProperty("document_binary_url")
    private String binaryUrl;

    public CcdDocument() {
    }

    public CcdDocument(String url, String fileName, String binaryUrl) {
        this.url = url;
        this.fileName = fileName;
        this.binaryUrl = binaryUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getBinaryUrl() {
        return binaryUrl;
    }

    public void setBinaryUrl(String binaryUrl) {
        this.binaryUrl = binaryUrl;
    }
}
