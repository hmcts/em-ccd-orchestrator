package uk.gov.hmcts.reform.em.orchestrator.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BundleDTO implements Serializable {

    @JsonIgnore
    private Long id;

    private String bundleTitle;
    private int version;
    private String description;
    private String purpose;
    private String stitchedDocId;
    private String stitchedDocumentURI;
    private String stitchStatus;
    private boolean isLocked;
    private Instant dateLocked;
    private String lockedBy;
    private String comments;
    private List<CcdValue<BundleFolderDTO>> folders = new ArrayList<>();
    private List<CcdValue<BundleDocumentDTO>> documents = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBundleTitle() {
        return bundleTitle;
    }

    public void setBundleTitle(String bundleTitle) {
        this.bundleTitle = bundleTitle;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStitchedDocId() {
        return stitchedDocId;
    }

    public void setStitchedDocId(String stitchedDocId) {
        this.stitchedDocId = stitchedDocId;
    }

    public String getStitchedDocumentURI() {
        return stitchedDocumentURI;
    }

    public void setStitchedDocumentURI(String stitchedDocumentURI) {
        this.stitchedDocumentURI = stitchedDocumentURI;
    }

    public String getStitchStatus() {
        return stitchStatus;
    }

    public void setStitchStatus(String stitchStatus) {
        this.stitchStatus = stitchStatus;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public Instant getDateLocked() {
        return dateLocked;
    }

    public void setDateLocked(Instant dateLocked) {
        this.dateLocked = dateLocked;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<CcdValue<BundleFolderDTO>> getFolders() {
        return folders;
    }

    public void setFolders(List<CcdValue<BundleFolderDTO>> folders) {
        this.folders = folders;
    }

    public List<CcdValue<BundleDocumentDTO>> getDocuments() {
        return documents;
    }

    public void setDocuments(List<CcdValue<BundleDocumentDTO>> documents) {
        this.documents = documents;
    }

}

