package uk.gov.hmcts.reform.em.orchestrator.functional.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
public class CcdTestBundleDocumentDTO implements Serializable {

    private String documentName;
    @SuppressWarnings("java:S1948")
    private CcdTestDocument documentLink;

}
