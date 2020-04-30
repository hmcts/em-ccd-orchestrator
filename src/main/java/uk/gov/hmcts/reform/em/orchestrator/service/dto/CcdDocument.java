package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class CcdDocument {

    @JsonProperty("document_url")
    private String url;
    @JsonProperty("document_filename")
    private String fileName;
    @JsonProperty("document_binary_url")
    private String binaryUrl;
    @JsonIgnore
    private LocalDateTime createdDatetime;

    public CcdDocument() {
    }

    public CcdDocument(String url, String fileName, String binaryUrl) {
        this.url = url;
        this.fileName = ensurePdfExtension(fileName);
        this.binaryUrl = binaryUrl;
    }

    public CcdDocument(String url, String fileName, String binaryUrl, LocalDateTime createdDatetime) {
        this.url = url;
        this.fileName = ensurePdfExtension(fileName);
        this.binaryUrl = binaryUrl;
        this.createdDatetime = createdDatetime;
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
        this.fileName = ensurePdfExtension(fileName);
    }

    public String getBinaryUrl() {
        return binaryUrl;
    }

    public void setBinaryUrl(String binaryUrl) {
        this.binaryUrl = binaryUrl;
    }

    public LocalDateTime getCreatedDatetime() {
        return createdDatetime;
    }

    public void setCreatedDatetime(LocalDateTime createdDatetime) {
        this.createdDatetime = createdDatetime;
    }

    private String ensurePdfExtension(String fileName) {
        return fileName.endsWith(".pdf") ? fileName : fileName.concat(".pdf");
    }
}
