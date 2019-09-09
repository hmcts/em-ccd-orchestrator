package uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler;

import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;

import java.util.UUID;

public class StitchingCompleteCallbackDto {

    private String jwt;
    private String caseId;
    private String triggerId;
    private UUID ccdBundleId;
    private DocumentTaskDTO documentTaskDTO;

    public StitchingCompleteCallbackDto(String jwt, String caseId, String triggerId, UUID ccdBundleId, DocumentTaskDTO documentTaskDTO) {
        this.jwt = jwt;
        this.caseId = caseId;
        this.triggerId = triggerId;
        this.ccdBundleId = ccdBundleId;
        this.documentTaskDTO = documentTaskDTO;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public DocumentTaskDTO getDocumentTaskDTO() {
        return documentTaskDTO;
    }

    public void setDocumentTaskDTO(DocumentTaskDTO documentTaskDTO) {
        this.documentTaskDTO = documentTaskDTO;
    }

    public UUID getCcdBundleId() {
        return ccdBundleId;
    }

    public void setCcdBundleId(UUID ccdBundleId) {
        this.ccdBundleId = ccdBundleId;
    }
}
