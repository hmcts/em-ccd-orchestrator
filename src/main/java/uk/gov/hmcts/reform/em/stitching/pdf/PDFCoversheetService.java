package uk.gov.hmcts.reform.em.stitching.pdf;

import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.springframework.stereotype.Service;

import static uk.gov.hmcts.reform.em.stitching.pdf.PDFUtility.*;

@Service
public class PDFCoversheetService {

    public PDDocument addCoversheet(File file) throws IOException {
        PDDocument document = PDDocument.load(file);
        PDPage coversheet = new PDPage();

        document.addPage(coversheet);
        document.getDocumentInformation().setCustomMetadataValue(PDF_META_FILENAME, file.getName());

        addCenterText(document, coversheet, getDocumentTitle(document));
        moveLastPageToFirst(document);

        return document;
    }

    private void moveLastPageToFirst(PDDocument document) {
        PDPageTree allPages = document.getDocumentCatalog().getPages();
        if (allPages.getCount() > 1) {
            PDPage lastPage = allPages.get(allPages.getCount() - 1);
            allPages.remove(allPages.getCount() - 1);
            PDPage firstPage = allPages.get(0);
            allPages.insertBefore(lastPage, firstPage);
        }
    }

}