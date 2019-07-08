package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Parsed configuration file for automated bundles.
 */
public class BundleConfiguration {

    public final String title;
    public final String filename;
    public final boolean hasTableOfContents;
    public final boolean hasCoversheets;
    public final boolean hasFolderCoversheets;
    public final List<BundleConfigurationFolder> folders;

    public BundleConfiguration(@JsonProperty("title") String title,
                               @JsonProperty("filename") String filename,
                               @JsonProperty("hasTableOfContents") boolean hasTableOfContents,
                               @JsonProperty("hasCoversheets") boolean hasCoversheets,
                               @JsonProperty("hasFolderCoversheets") boolean hasFolderCoversheets,
                               @JsonProperty("folders") List<BundleConfigurationFolder> folders) {
        this.title = title;
        this.filename = filename == null ? "stitched.pdf" : filename;
        this.hasTableOfContents = hasTableOfContents;
        this.hasCoversheets = hasCoversheets;
        this.hasFolderCoversheets = hasFolderCoversheets;
        this.folders = folders;
    }
}
