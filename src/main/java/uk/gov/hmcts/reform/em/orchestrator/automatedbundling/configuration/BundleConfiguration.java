package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Parsed configuration file for automated bundles.
 */
public class BundleConfiguration {

    public final String title;
    public final String filename;
    public final List<BundleConfigurationFolder> folders;

    public BundleConfiguration(@JsonProperty("title") String title,
                               @JsonProperty(value = "filename") String filename,
                               @JsonProperty("folders") List<BundleConfigurationFolder> folders) {
        this.title = title;
        this.filename = filename == null ? "stitched.pdf" : filename;
        this.folders = folders;
    }
}
