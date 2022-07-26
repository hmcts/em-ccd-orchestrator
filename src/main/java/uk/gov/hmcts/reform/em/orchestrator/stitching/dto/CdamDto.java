package uk.gov.hmcts.reform.em.orchestrator.stitching.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CdamDto {

    private String jwt;
    private String caseId;
    private String caseTypeId;
    private String jurisdictionId;

}
