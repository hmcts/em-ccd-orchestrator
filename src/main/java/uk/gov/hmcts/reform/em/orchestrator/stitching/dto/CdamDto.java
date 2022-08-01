package uk.gov.hmcts.reform.em.orchestrator.stitching.dto;

import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Builder
@Getter
public class CdamDto implements Serializable {

    private String jwt;
    private String caseId;
    private String caseTypeId;
    private String jurisdictionId;
    private String serviceAuth;
}
