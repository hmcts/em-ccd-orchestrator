package uk.gov.hmcts.reform.em.stitching.service.impl;

import okhttp3.*;
import org.apache.pdfbox.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.stitching.Application;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@Transactional
public class DocumentConversionServiceImplTest {

    private static final String PDF_FILENAME = "annotationTemplate.pdf";

    private DocumentConversionServiceImpl conversionService;

    @Before
    public void setup() {
        OkHttpClient okHttpClient = new OkHttpClient
            .Builder()
            .addInterceptor(DocumentConversionServiceImplTest::intercept)
            .build();

        conversionService = new DocumentConversionServiceImpl("key", "http://example.org", okHttpClient);
    }

    private static Response intercept(Interceptor.Chain chain) throws IOException{
        InputStream file = ClassLoader.getSystemResourceAsStream(PDF_FILENAME);

        return new Response.Builder()
            .body(ResponseBody.create(MediaType.get("application/pdf"), IOUtils.toByteArray(file)))
            .request(chain.request())
            .message("")
            .code(200)
            .protocol(Protocol.HTTP_2)
            .build();
    }

    @Test
    public void dontConvertPDFs() throws IOException {
        File input = new File(ClassLoader.getSystemResource(PDF_FILENAME).getPath());
        File output = conversionService.convert(input);

        assertEquals(input, output);
    }

    @Test
    public void convertWordDocument() throws IOException {
        String filename = ClassLoader.getSystemResource("wordDocument.doc").getPath();
        File input = new File(filename);
        File output = conversionService.convert(input);

        assertNotEquals(input.getName(), output.getName());
    }

}