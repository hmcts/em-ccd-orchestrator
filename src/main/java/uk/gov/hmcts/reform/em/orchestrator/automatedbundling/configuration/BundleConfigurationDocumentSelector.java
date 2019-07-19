package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BundleConfigurationDocumentSet.class, name = "documentSet"),
    @JsonSubTypes.Type(value = BundleConfigurationDocument.class, name = "document")
})
public interface BundleConfigurationDocumentSelector {

}
