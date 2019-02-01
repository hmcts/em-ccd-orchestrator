package uk.gov.hmcts.reform.em.stitching.batch;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import pl.touk.throwing.ThrowingFunction;
import uk.gov.hmcts.reform.em.stitching.domain.DocumentTask;
import uk.gov.hmcts.reform.em.stitching.domain.enumeration.TaskState;
import uk.gov.hmcts.reform.em.stitching.pdf.PDFCoversheetService;
import uk.gov.hmcts.reform.em.stitching.pdf.PDFMerger;
import uk.gov.hmcts.reform.em.stitching.service.DmStoreDownloader;
import uk.gov.hmcts.reform.em.stitching.service.DmStoreUploader;
import uk.gov.hmcts.reform.em.stitching.service.DocumentConversionService;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class DocumentTaskItemProcessor implements ItemProcessor<DocumentTask, DocumentTask> {

    private final Logger log = LoggerFactory.getLogger(DocumentTaskItemProcessor.class);
    private final DmStoreDownloader dmStoreDownloader;
    private final DmStoreUploader dmStoreUploader;
    private final DocumentConversionService documentConverter;
    private final PDFCoversheetService coversheetService;
    private final PDFMerger pdfMerger;

    public DocumentTaskItemProcessor(DmStoreDownloader dmStoreDownloader,
                                     DmStoreUploader dmStoreUploader,
                                     DocumentConversionService documentConverter,
                                     PDFCoversheetService coversheetService,
                                     PDFMerger pdfMerger) {
        this.dmStoreDownloader = dmStoreDownloader;
        this.dmStoreUploader = dmStoreUploader;
        this.documentConverter = documentConverter;
        this.coversheetService = coversheetService;
        this.pdfMerger = pdfMerger;
    }

    @Override
    public DocumentTask process(DocumentTask item) {
        try {
            List<PDDocument> documents = dmStoreDownloader
                .downloadFiles(item.getBundle().getSortedItems())
                .map(ThrowingFunction.unchecked(documentConverter::convert))
                .map(ThrowingFunction.unchecked(coversheetService::addCoversheet))
                .collect(Collectors.toList());

            final File outputFile = pdfMerger.merge(documents);

            dmStoreUploader.uploadFile(outputFile, item);

            item.setTaskState(TaskState.DONE);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);

            item.setTaskState(TaskState.FAILED);
            item.setFailureDescription(e.getMessage());
        }

        return item;
    }

}
