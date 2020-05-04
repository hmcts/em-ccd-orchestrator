package uk.gov.hmcts.reform.em.orchestrator.stitching;

import org.springframework.util.StringUtils;

import java.util.Objects;

public class StitchingStringUtil {

    public static String ensurePdfExtension(String fileName) {
        if (Objects.nonNull(fileName) && !StringUtils.isEmpty(fileName)) {
            if (StringUtils.getFilenameExtension(fileName) != null) {
                return fileName;
            } else {
                return fileName.concat(".pdf");
            }
        } else {
            return "stitched.pdf";
        }

    }
}
