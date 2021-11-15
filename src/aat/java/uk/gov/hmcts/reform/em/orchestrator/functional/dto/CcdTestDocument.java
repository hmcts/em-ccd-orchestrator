package uk.gov.hmcts.reform.em.orchestrator.functional.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class CcdTestDocument {

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

}
