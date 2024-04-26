package uk.gov.hmcts.reform.em.orchestrator.stitching;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities;

public class StitchingServiceException extends RuntimeException {

    public StitchingServiceException(String message) {
        super(message);
    }

    public StitchingServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public StitchingServiceException(String caseId, String message, Throwable cause) {
        super(message, cause);

        String errorLog = String.format("Stitching Failed for caseId : %s with issue : %s ",
                StringUtilities.convertValidLog(caseId),
                StringUtilities.convertValidLog(message));

        Logger logger = LoggerFactory.getLogger(StitchingServiceException.class);
        logger.error(errorLog);
    }
}
