package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

public class CaseUpdaterDoesNotExistException extends RuntimeException {

    public CaseUpdaterDoesNotExistException(String message) {
        super(message);
    }
}
