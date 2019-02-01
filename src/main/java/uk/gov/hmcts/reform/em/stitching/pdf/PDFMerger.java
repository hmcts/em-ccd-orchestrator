package uk.gov.hmcts.reform.em.stitching.pdf;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static uk.gov.hmcts.reform.em.stitching.pdf.PDFUtility.addCenterText;
import static uk.gov.hmcts.reform.em.stitching.pdf.PDFUtility.getDocumentTitle;

@Service
public class PDFMerger {

    public File merge(List<PDDocument> documents) throws IOException {
        StatefulPDFMerger statefulPDFMerger = new StatefulPDFMerger();

        return statefulPDFMerger.merge(documents);
    }

    private class StatefulPDFMerger {
        private static final int LINE_HEIGHT = 9;
        private final PDFMergerUtility merger = new PDFMergerUtility();
        private final PDDocument document = new PDDocument();
        private final PDPage page = new PDPage();
        private int currentPageNumber = 1;

        public StatefulPDFMerger() {
            document.addPage(page);
        }

        public File merge(List<PDDocument> documents) throws IOException {
            addCenterText(document, page, "Table of contents");

            for (PDDocument d : documents) {
                add(d);
            }

            final File file = File.createTempFile("stitched", ".pdf");

            document.save(file);
            document.close();

            return file;
        }

        private void add(PDDocument newDoc) throws IOException {
            merger.appendDocument(document, newDoc);

            addTableOfContentsItem(getDocumentTitle(newDoc));

            currentPageNumber += newDoc.getNumberOfPages();
        }

        private void addTableOfContentsItem(String documentTitle) throws IOException {
            final PDPageXYZDestination destination = new PDPageXYZDestination();
            destination.setPage(document.getPage(currentPageNumber));

            final PDActionGoTo action = new PDActionGoTo();
            action.setDestination(destination);

            final float yOffset = (float) currentPageNumber * LINE_HEIGHT;
            final PDRectangle rectangle = new PDRectangle(45, 700 - yOffset, 200, LINE_HEIGHT);

            final PDBorderStyleDictionary underline = new PDBorderStyleDictionary();
            underline.setStyle(PDBorderStyleDictionary.STYLE_UNDERLINE);

            final PDAnnotationLink link = new PDAnnotationLink();
            link.setAction(action);
            link.setDestination(destination);
            link.setRectangle(rectangle);
            link.setBorderStyle(underline);

            page.getAnnotations().add(link);

            final PDPageContentStream stream = new PDPageContentStream(document, page, AppendMode.APPEND, true);
            stream.beginText();
            stream.setFont(PDType1Font.HELVETICA, 10);
            stream.newLineAtOffset(50, 701 - yOffset);
            stream.showText(documentTitle + ", p" + (currentPageNumber + 1));
            stream.endText();
            stream.close();
        }
    }
}
