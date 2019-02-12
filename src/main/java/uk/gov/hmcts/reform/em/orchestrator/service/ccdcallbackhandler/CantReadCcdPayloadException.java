package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

public class CantReadCcdPayloadException extends RuntimeException {

    public CantReadCcdPayloadException(String message) {
        super(message);
    }

    public CantReadCcdPayloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
