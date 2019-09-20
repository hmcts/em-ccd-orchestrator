package uk.gov.hmcts.reform.em.orchestrator.testutil;

public class HttpHelper {

    public static boolean isSuccessful(int code) {
        return code >= 200 && code < 300;
    }
}
