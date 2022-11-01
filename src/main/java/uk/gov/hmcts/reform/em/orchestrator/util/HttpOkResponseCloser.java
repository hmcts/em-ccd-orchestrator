package uk.gov.hmcts.reform.em.orchestrator.util;

import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpOkResponseCloser {

    private static Logger log = LoggerFactory.getLogger(HttpOkResponseCloser.class);

    public static void closeResponse(Response response) {
        try {
            response.close();
        } catch (Exception ex) {
            log.info("Closing http response failed.", ex);
        }
    }
}
