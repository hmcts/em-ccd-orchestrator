package uk.gov.hmcts.reform.em.stitching.pdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static uk.gov.hmcts.reform.em.stitching.pdf.PDFUtility.PDF_META_FILENAME;

public class PDFMergerTest {
    private static final File FILE_1 = new File(
        ClassLoader.getSystemResource("TEST_INPUT_FILE.pdf").getPath()
    );

    private static final File FILE_2 = new File(
        ClassLoader.getSystemResource("annotationTemplate.pdf").getPath()
    );

    private List<PDDocument> documents;
    private PDDocument document1;
    private PDDocument document2;

    @Before
    public void setup() throws IOException {
        document1 = PDDocument.load(FILE_1);
        document1.getDocumentInformation().setCustomMetadataValue(PDF_META_FILENAME, FILE_1.getName());
        document2 = PDDocument.load(FILE_2);
        documents = new ArrayList<>();

        documents.add(document1);
        documents.add(document2);
    }

    @Test
    public void merge() throws IOException {
        PDFMerger merger = new PDFMerger();
        File merged = merger.merge(documents);
        PDDocument mergedDocument = PDDocument.load(merged);
        int expectedPages = document1.getNumberOfPages() + document2.getNumberOfPages() + 1;

        assertEquals(expectedPages, mergedDocument.getNumberOfPages());
    }

}