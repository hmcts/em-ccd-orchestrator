package uk.gov.hmcts.reform.em.stitching.service.callback.impl;

public class IncorrectCcdCaseBundlesException extends RuntimeException {

    public IncorrectCcdCaseBundlesException(String message, Throwable cause) {
        super(message, cause);
    }
}
