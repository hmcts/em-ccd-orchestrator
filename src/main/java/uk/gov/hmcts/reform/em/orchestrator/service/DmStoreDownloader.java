package uk.gov.hmcts.reform.em.orchestrator.service;

import uk.gov.hmcts.reform.em.orchestrator.domain.BundleDocument;
import uk.gov.hmcts.reform.em.orchestrator.service.impl.DocumentTaskProcessingException;

import java.io.File;
import java.util.stream.Stream;

public interface DmStoreDownloader {

    Stream<File> downloadFiles(Stream<BundleDocument> bundleDocuments) throws DocumentTaskProcessingException;

}
