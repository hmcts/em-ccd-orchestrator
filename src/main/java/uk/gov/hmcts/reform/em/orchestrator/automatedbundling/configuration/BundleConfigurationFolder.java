package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Configuration for a bundle folder
 */
public class BundleConfigurationFolder {

    public final String name;
    public final List<BundleConfigurationFolder> folders;

    public BundleConfigurationFolder(@JsonProperty("name") String name,
                                     @JsonProperty("folders") List<BundleConfigurationFolder> folders) {
        this.name = name;
        this.folders = folders;
    }
}
