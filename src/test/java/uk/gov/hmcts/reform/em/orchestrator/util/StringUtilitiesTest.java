package uk.gov.hmcts.reform.em.orchestrator.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@ExtendWith(MockitoExtension.class)
class StringUtilitiesTest {

    private static final String FILE_NAME_WITH_EXTENSION = "sampletest.pdf";
    private static final String FILE_NAME_WITH_OUT_EXTENSION = "sampletest";
    private static final String DEFAULT_FILE_NAME = "stitched.pdf";

    @Test
    void ensurePdfExtensionWithExtension() {
        assertEquals(FILE_NAME_WITH_EXTENSION, StringUtilities.ensurePdfExtension(FILE_NAME_WITH_EXTENSION));
    }

    @Test
    void ensurePdfExtensionWithOutExtension() {
        assertEquals(FILE_NAME_WITH_EXTENSION, StringUtilities.ensurePdfExtension(FILE_NAME_WITH_OUT_EXTENSION));
    }

    @Test
    void ensurePdfExtensionWithOutFileName() {
        assertEquals(DEFAULT_FILE_NAME, StringUtilities.ensurePdfExtension(null));
    }

    @Test
    void convertValidLog() {
        String dangerousLogStr = "this %0d is \r an %0a apple \n .";
        String safeLogStr = "this  is  an  apple  .";
        assertNotEquals(dangerousLogStr, safeLogStr);
        assertEquals(StringUtilities.convertValidLog(dangerousLogStr), safeLogStr);
    }

}
