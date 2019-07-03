package uk.gov.hmcts.reform.em.orchestrator.service.automatedbundling;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration for a bundle folder
 */
public class BundleConfigurationFolder {

    public final String name;

    public BundleConfigurationFolder(@JsonProperty("name") String name) {
        this.name = name;
    }
}
