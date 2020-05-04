package uk.gov.hmcts.reform.em.orchestrator.util;

import org.apache.commons.io.FilenameUtils;
import org.springframework.util.StringUtils;

import java.util.Objects;

public class StringUtilities {

    private StringUtilities() {
    }

    public static String ensurePdfExtension(String fileName) {
        if (Objects.nonNull(fileName) && !StringUtils.isEmpty(fileName)) {
            if (!FilenameUtils.getExtension(fileName).isEmpty()) {
                return fileName;
            } else {
                return fileName.concat(".pdf");
            }
        } else {
            return "stitched.pdf";
        }

    }
}
