package uk.gov.hmcts.reform.em.orchestrator.testutil;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

public class HttpHelper {

    private static final Logger log = LoggerFactory.getLogger(HttpHelper.class);

    public static boolean isSuccessful(int code) {
        boolean success = code >= 200 && code < 300;
        if (!success) {
            log.info(String.format("HTTP request was not successful, it was %d", code));
        }
        return code >= 200 && code < 300;
    }
}
