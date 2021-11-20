package uk.gov.hmcts.reform.em.orchestrator.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;


public class StringUtilities {

    private StringUtilities() {
    }

    public static String ensurePdfExtension(String fileName) {
        if (StringUtils.isNoneBlank(fileName)) {
            if (!FilenameUtils.getExtension(fileName).isEmpty()) {
                return fileName;
            } else {
                return fileName.concat(".pdf");
            }
        } else {
            return "stitched.pdf";
        }

    }

    public static String convertValidLog(String log) {
        if (StringUtils.isNoneBlank(log)) {
            List<String> list = Arrays.asList("%0d", "\r", "%0a", "\n");

            // normalize the log content
            String encode = Normalizer.normalize(log, Normalizer.Form.NFKC);
            for (String toReplaceStr : list) {
                encode = encode.replace(toReplaceStr, "");
            }
            return encode;
        }
        return null;
    }

}
