package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
public class CcdDocument implements Serializable {

    @JsonProperty("document_url")
    private String url;
    @JsonProperty("document_filename")
    private String fileName;
    @JsonProperty("document_binary_url")
    private String binaryUrl;
    @JsonProperty("document_hash")
    private String hash;

    @JsonIgnore
    private LocalDateTime createdDatetime;

    public CcdDocument() {
    }

    public CcdDocument(String url, String fileName, String binaryUrl) {
        this.url = url;
        this.fileName = fileName;
        this.binaryUrl = binaryUrl;
    }

    public CcdDocument(String url, String fileName, String binaryUrl, String hash) {
        this.url = url;
        this.fileName = fileName;
        this.binaryUrl = binaryUrl;
        this.hash = hash;
    }

    public CcdDocument(String url, String fileName, String binaryUrl, LocalDateTime createdDatetime) {
        this.url = url;
        this.fileName = fileName;
        this.binaryUrl = binaryUrl;
        this.createdDatetime = createdDatetime;
    }

    public CcdDocument(String url, String fileName, String binaryUrl, String hash, LocalDateTime createdDatetime) {
        this.url = url;
        this.fileName = fileName;
        this.binaryUrl = binaryUrl;
        this.createdDatetime = createdDatetime;
        this.hash = hash;
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

    public LocalDateTime getCreatedDatetime() {
        return createdDatetime;
    }

    public void setCreatedDatetime(LocalDateTime createdDatetime) {
        this.createdDatetime = createdDatetime;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
