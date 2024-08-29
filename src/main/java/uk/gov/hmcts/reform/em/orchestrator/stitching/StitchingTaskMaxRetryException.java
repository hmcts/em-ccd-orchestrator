package uk.gov.hmcts.reform.em.orchestrator.stitching;

public class StitchingTaskMaxRetryException extends RuntimeException {

    public StitchingTaskMaxRetryException(String taskId) {
        super("Task not complete after maximum number of retries for DocumentTaskId : ".concat(taskId));
    }
}
