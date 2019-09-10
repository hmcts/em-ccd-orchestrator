package uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler;

import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;

import java.util.UUID;

public class StitchingCompleteCallbackDto {

    private final String jwt;
    private final String caseId;
    private final String triggerId;
    private final UUID ccdBundleId;
    private final DocumentTaskDTO documentTaskDTO;

    public StitchingCompleteCallbackDto(String jwt, String caseId, String triggerId, UUID ccdBundleId, DocumentTaskDTO documentTaskDTO) {
        this.jwt = jwt;
        this.caseId = caseId;
        this.triggerId = triggerId;
        this.ccdBundleId = ccdBundleId;
        this.documentTaskDTO = documentTaskDTO;
    }

    public String getJwt() {
        return jwt;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getTriggerId() {
        return triggerId;
    }

    public DocumentTaskDTO getDocumentTaskDTO() {
        return documentTaskDTO;
    }

    public UUID getCcdBundleId() {
        return ccdBundleId;
    }

}
