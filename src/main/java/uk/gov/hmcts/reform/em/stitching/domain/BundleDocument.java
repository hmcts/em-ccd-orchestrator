package uk.gov.hmcts.reform.em.stitching.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.stream.Stream;

@Entity
@Table(name = "bundle_document")
public class BundleDocument extends AbstractAuditingEntity implements SortableBundleItem, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    private String documentId;
    private String docTitle;
    private String docDescription;
    private String documentURI;
    private Instant dateAddedToCase;
    private boolean isIncludedInBundle;
    private String creatorRole;
    private int sortIndex;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    public String getDocDescription() {
        return docDescription;
    }

    public void setDocDescription(String docDescription) {
        this.docDescription = docDescription;
    }

    public String getDocumentURI() {
        return documentURI;
    }

    public void setDocumentURI(String documentURI) {
        this.documentURI = documentURI;
    }

    public Instant getDateAddedToCase() {
        return dateAddedToCase;
    }

    public void setDateAddedToCase(Instant dateAddedToCase) {
        this.dateAddedToCase = dateAddedToCase;
    }

    public String getCreatorRole() {
        return creatorRole;
    }

    public void setCreatorRole(String creatorRole) {
        this.creatorRole = creatorRole;
    }

    public boolean isIncludedInBundle() {
        return isIncludedInBundle;
    }

    public void setIncludedInBundle(boolean includedInBundle) {
        isIncludedInBundle = includedInBundle;
    }

    @Override
    @Transient
    public Stream<BundleDocument> getSortedItems() {
        return Stream.of(this);
    }

    @Override
    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }
}