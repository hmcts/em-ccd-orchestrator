package uk.gov.hmcts.reform.em.stitching.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;

public class PDFUtility {

    public static final String PDF_META_FILENAME = "filename";

    private PDFUtility() {
        
    }

    public static void addCenterText(PDDocument document, PDPage page, String text) throws IOException {
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        int fontSize = 20;
        PDFont font = PDType1Font.HELVETICA_BOLD;
        contentStream.setFont(font, fontSize);

        float stringWidth = updateStringWidth(text, font, fontSize);
        final float titleHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
        final float titleWidth = font.getStringWidth(text) / 1000 * fontSize;
        final float pageHeight = page.getMediaBox().getHeight();
        final float pageWidth = page.getMediaBox().getWidth();

        while (stringWidth > pageWidth) {
            fontSize = fontSize - 2;
            stringWidth = updateStringWidth(text, font, fontSize);
        }

        contentStream.beginText();
        contentStream.newLineAtOffset((pageWidth - titleWidth) / 2, pageHeight - 20 - titleHeight);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.close();
    }

    private static float updateStringWidth(String string, PDFont font, int fontSize) throws IOException{
        return font.getStringWidth(string) / 1000 * fontSize;
    }

    public static String getDocumentTitle(PDDocument document) {
        return document.getDocumentInformation().getTitle() != null
                ? document.getDocumentInformation().getTitle()
                : document.getDocumentInformation().getCustomMetadataValue(PDF_META_FILENAME);
    }

}
