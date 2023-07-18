package uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class CallbackException extends RuntimeException {

    private final int httpStatus;
    private final String httpResponseBody;

    public CallbackException(int httpStatus, String httpResponseBody, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.httpResponseBody = httpResponseBody;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getHttpResponseBody() {
        return httpResponseBody;
    }

    @Override
    public String toString() {
        return String.format("CallbackException(%s,%d, %s)", getMessage(), getHttpStatus(), getHttpResponseBody());
    }
    @ResponseStatus(HttpStatus.CONFLICT)
    public static CallbackException conflict(String httpResponseBody, String message) {
        return new CallbackException(HttpStatus.CONFLICT.value(), httpResponseBody, message);
    }


}
