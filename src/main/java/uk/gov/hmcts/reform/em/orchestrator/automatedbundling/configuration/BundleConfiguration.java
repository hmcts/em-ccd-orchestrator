package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Parsed configuration file for automated bundles.
 */
public class BundleConfiguration {

    public final String title;
    public final List<BundleConfigurationFolder> folders;

    public BundleConfiguration(@JsonProperty("title") String title,
                               @JsonProperty("folders") List<BundleConfigurationFolder> folders) {
        this.title = title;
        this.folders = folders;
    }
}
