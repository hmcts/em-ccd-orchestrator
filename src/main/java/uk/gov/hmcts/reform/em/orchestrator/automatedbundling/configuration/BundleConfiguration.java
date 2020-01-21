package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundlePaginationStyle;
import uk.gov.hmcts.reform.em.orchestrator.domain.enumeration.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Parsed configuration file for automated bundles.
 */
@SuppressWarnings("all")
public class BundleConfiguration {

    public final String title;
    public final String filename;
    public final String coverpageTemplate;
    public final PageNumberFormat pageNumberFormat;
    public final BundleConfigurationSortOrder sortOrder;
    public final boolean hasTableOfContents;
    public final boolean hasCoversheets;
    public final boolean hasFolderCoversheets;
    public final List<BundleConfigurationFolder> folders;
    public final List<BundleConfigurationDocumentSelector> documents;
    public final CcdBundlePaginationStyle paginationStyle;
    public final String documentNameValue;

    public BundleConfiguration(@JsonProperty("title") String title,
                               @JsonProperty("filename") String filename,
                               @JsonProperty("coverpageTemplate") String coverpageTemplate,
                               @JsonProperty("pageNumberFormat") PageNumberFormat pageNumberFormat,
                               @JsonProperty("sort") BundleConfigurationSortOrder sortOrder,
                               @JsonProperty("hasTableOfContents") boolean hasTableOfContents,
                               @JsonProperty("hasCoversheets") boolean hasCoversheets,
                               @JsonProperty("hasFolderCoversheets") boolean hasFolderCoversheets,
                               @JsonProperty("folders") List<BundleConfigurationFolder> folders,
                               @JsonProperty("documents") List<BundleConfigurationDocumentSelector> documents,
                               @JsonProperty("paginationStyle") CcdBundlePaginationStyle paginationStyle,
                               @JsonProperty("documentNameValue") String documentNameValue) {
        this.title = title;
        this.filename = filename == null ? "stitched.pdf" : filename;
        this.coverpageTemplate = coverpageTemplate == null ? "" : coverpageTemplate;
        this.pageNumberFormat = pageNumberFormat;
        this.sortOrder = sortOrder;
        this.hasTableOfContents = hasTableOfContents;
        this.hasCoversheets = hasCoversheets;
        this.hasFolderCoversheets = hasFolderCoversheets;
        this.folders = folders == null ? new ArrayList<>() : folders;
        this.documents = documents == null ? new ArrayList<>() : documents;
        this.paginationStyle = paginationStyle;
        this.documentNameValue = documentNameValue;
    }
}
