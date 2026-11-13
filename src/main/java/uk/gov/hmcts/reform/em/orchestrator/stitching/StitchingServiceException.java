package uk.gov.hmcts.reform.em.orchestrator.stitching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities;

public class StitchingServiceException extends RuntimeException {

    private static final Logger logger = LoggerFactory.getLogger(StitchingServiceException.class);

    public StitchingServiceException(String message) {
        super(message);
    }

    public StitchingServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public StitchingServiceException(String caseId, String message, Throwable cause) {
        super(message, cause);

        logger.error("Stitching Failed for caseId : {} with issue : {} ",
                StringUtilities.convertValidLog(caseId),
                StringUtilities.convertValidLog(message));
    }
}
