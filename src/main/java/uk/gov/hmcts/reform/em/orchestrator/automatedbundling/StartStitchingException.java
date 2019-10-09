package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

public class StartStitchingException extends RuntimeException {

    public StartStitchingException(String error, Throwable e) {
        super(error, e);
    }
}
