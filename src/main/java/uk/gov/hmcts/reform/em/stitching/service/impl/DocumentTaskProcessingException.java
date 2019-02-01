package uk.gov.hmcts.reform.em.stitching.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DocumentTaskProcessingException extends Exception {

    public DocumentTaskProcessingException(String message) {
        super(message);
    }

    public DocumentTaskProcessingException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
