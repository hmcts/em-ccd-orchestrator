package uk.gov.hmcts.reform.em.orchestrator.util;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class StringUtilitiesTest {

    private static final String FILE_NAME_WITH_EXTENSION = "sampletest.pdf";
    private static final String FILE_NAME_WITH_OUT_EXTENSION = "sampletest";
    private static final String DEFAULT_FILE_NAME = "stitched.pdf";

    @Test
    public void ensurePdfExtensionWithExtension() {
        Assert.assertEquals(FILE_NAME_WITH_EXTENSION, StringUtilities.ensurePdfExtension(FILE_NAME_WITH_EXTENSION));
    }

    @Test
    public void ensurePdfExtensionWithOutExtension() {
        Assert.assertEquals(FILE_NAME_WITH_EXTENSION, StringUtilities.ensurePdfExtension(FILE_NAME_WITH_OUT_EXTENSION));
    }

    @Test
    public void ensurePdfExtensionWithOutFileName() {
        Assert.assertEquals(DEFAULT_FILE_NAME, StringUtilities.ensurePdfExtension(null));
    }

    @Test
    public void convertValidLog() {
        String dangerousLogStr = "this %0d is \r an %0a apple \n .";
        String safeLogStr = "this  is  an  apple  .";
        Assert.assertNotEquals(dangerousLogStr, safeLogStr);
        Assert.assertEquals(StringUtilities.convertValidLog(dangerousLogStr), safeLogStr);
    }

}
