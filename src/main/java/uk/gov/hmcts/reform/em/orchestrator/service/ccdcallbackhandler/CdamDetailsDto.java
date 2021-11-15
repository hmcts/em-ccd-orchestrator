package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Builder
@Data
@ToString
public class CdamDetailsDto implements Serializable {

    private String caseTypeId;
    private String jurisdictionId;
    private String serviceAuth;

}
