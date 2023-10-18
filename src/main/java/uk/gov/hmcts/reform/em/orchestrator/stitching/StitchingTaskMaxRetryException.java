package uk.gov.hmcts.reform.em.orchestrator.stitching;

public class StitchingTaskMaxRetryException extends RuntimeException{

    public StitchingTaskMaxRetryException() {
        super("Task not complete after maximum number of retries");
    }
}
