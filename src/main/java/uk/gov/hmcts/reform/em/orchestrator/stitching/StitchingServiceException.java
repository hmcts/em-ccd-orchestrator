package uk.gov.hmcts.reform.em.orchestrator.stitching;

public class StitchingServiceException extends RuntimeException {

    public StitchingServiceException(String message) {
        super(message);
    }

    public StitchingServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
