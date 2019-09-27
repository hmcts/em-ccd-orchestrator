package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsed configuration file for automated bundles.
 */
public class BundleConfiguration {

    public final String title;
    public final String filename;
    public final PageNumberFormat pageNumberFormat;
    public final boolean hasTableOfContents;
    public final boolean hasCoversheets;
    public final boolean hasFolderCoversheets;
    public final List<BundleConfigurationFolder> folders;
    public final List<BundleConfigurationDocumentSelector> documents;

    public BundleConfiguration(@JsonProperty("title") String title,
                               @JsonProperty("filename") String filename,
                               @JsonProperty(value = "pageNumberFormat", defaultValue = "numberOfPages") PageNumberFormat pageNumberFormat,
                               @JsonProperty("hasTableOfContents") boolean hasTableOfContents,
                               @JsonProperty("hasCoversheets") boolean hasCoversheets,
                               @JsonProperty("hasFolderCoversheets") boolean hasFolderCoversheets,
                               @JsonProperty("folders") List<BundleConfigurationFolder> folders,
                               @JsonProperty("documents") List<BundleConfigurationDocumentSelector> documents) {
        this.title = title;
        this.filename = filename == null ? "stitched.pdf" : filename;
        this.pageNumberFormat = pageNumberFormat;
        this.hasTableOfContents = hasTableOfContents;
        this.hasCoversheets = hasCoversheets;
        this.hasFolderCoversheets = hasFolderCoversheets;
        this.folders = folders == null ? new ArrayList<>() : folders;
        this.documents = documents == null ? new ArrayList<>() : documents;
    }
}
