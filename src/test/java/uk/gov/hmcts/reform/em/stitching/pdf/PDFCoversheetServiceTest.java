package uk.gov.hmcts.reform.em.stitching.pdf;

import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.stitching.Application;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Transactional
public class PDFCoversheetServiceTest {


    private final PDFCoversheetService documentFormatter = new PDFCoversheetService();

    private static final String inputFileName = ClassLoader.getSystemResource("TEST_INPUT_FILE.pdf").getPath();
    private static final File INPUT_FILE = new File(inputFileName);
    private static PDDocument document;
    private static String documentName;

    @Before
    public void setup() throws IOException {
        document = PDDocument.load(INPUT_FILE);
        documentName = INPUT_FILE.getName();

    }

    @After
    public void teardown() throws IOException {
        document.close();
    }

    @Test
    public void addNewPageCountTest() throws Exception{
        int pageCountBefore = document.getNumberOfPages();
        PDDocument output = documentFormatter.addCoversheet(INPUT_FILE);
        int pageCountAfter = output.getNumberOfPages();
        assertEquals(pageCountBefore+ 1, pageCountAfter);
    }

    @Test
    public void addTextToDocumentTest() throws Exception {
        PDFTextStripper pdfStripper = new PDFTextStripper();

        String documentTextBefore = pdfStripper.getText(document);
        int nameCountBefore = StringUtils.countMatches(documentTextBefore, documentName);

        PDDocument output = documentFormatter.addCoversheet(INPUT_FILE);
        String documentTextAfter = pdfStripper.getText(output);
        int nameCountAfter = StringUtils.countMatches(documentTextAfter, documentName);

        assertEquals(nameCountBefore + 1, nameCountAfter);
    }

    @Test
    public void addTextToFirstPageTest() throws Exception {
        PDDocument output = documentFormatter.addCoversheet(INPUT_FILE);

        PDFTextStripper pdfStripper = new PDFTextStripper();
        pdfStripper.setStartPage(0);
        pdfStripper.setEndPage(1);
        String documentTextAfter = pdfStripper.getText(output);
        int nameCountOnFirstPage = StringUtils.countMatches(documentTextAfter, documentName);
        assertEquals(1, nameCountOnFirstPage);
    }

}