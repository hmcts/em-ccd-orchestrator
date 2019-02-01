package uk.gov.hmcts.reform.em.orchestrator.domain;

import java.util.stream.Stream;

public interface SortableBundleItem {

    Stream<BundleDocument> getSortedItems();

    int getSortIndex();

}
