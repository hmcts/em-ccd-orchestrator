package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

public class InvalidCaseException extends RuntimeException {

    public InvalidCaseException(String error) {
        super(error);
    }
}
