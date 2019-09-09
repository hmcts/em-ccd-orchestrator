package uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler;

public class CallbackException extends Exception {

    private final int httpStatus;
    private final String httpResponseBody;

    public CallbackException(int httpStatus, String httpResponseBody, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.httpResponseBody = httpResponseBody;
    }

    public CallbackException(int httpStatus, String httpResponseBody, String message, Throwable e) {
        super(message, e);
        this.httpStatus = httpStatus;
        this.httpResponseBody = httpResponseBody;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getHttpResponseBody() {
        return httpResponseBody;
    }

    public String toString() {
        return String.format("CallbackException(%s,%d, %s)", getMessage(), httpStatus, httpResponseBody);
    }

}
