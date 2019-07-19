package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BundleConfigurationDocument implements BundleConfigurationDocumentSelector {
    public final String property;

    public BundleConfigurationDocument(@JsonProperty("property") String property) {
        this.property = property;
    }

}
