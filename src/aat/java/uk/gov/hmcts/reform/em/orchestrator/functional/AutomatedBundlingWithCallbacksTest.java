package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.JsonNode;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class AutomatedBundlingWithCallbacksTest extends BaseTest {

    private static final int WAIT_SECONDS = 60;

    @BeforeEach
    public void setUp() {
        assumeFalse(enableCdamValidation);
    }

    @Test
    void testSuccessfulAsyncStitching() throws Exception {
        String uploadedUrl = testUtil.uploadDocument();
        String documentString = extendedCcdHelper.getCcdDocumentJson("my doc text", uploadedUrl, "mydoc.txt");
        String caseId = extendedCcdHelper.createCase(documentString).getId().toString();
        extendedCcdHelper.triggerEvent(caseId, "createBundle");

        Callable<JsonNode> callable = () -> extendedCcdHelper.getCase(caseId);
        Predicate<JsonNode> predicate = jsonNode ->
            !jsonNode.findPath("stitchStatus").asText().equals("NEW");

        Awaitility.await().pollInterval(1, TimeUnit.SECONDS)
            .atMost(WAIT_SECONDS, TimeUnit.SECONDS).until(callable, predicate);

        JsonNode caseJson = callable.call();
        if (caseJson.findPath("stitchStatus").asText().equals("NEW")) {
            fail("Status was not retrieved.");
        }
        assertEquals("DONE", caseJson.findPath("stitchStatus").asText());
        assertEquals("null", caseJson.findPath("stitchingFailureMessage").asText());
    }

    @Test
    void testUnSuccessfulAsyncStitching() throws Exception {
        String uploadedUrl = testUtil.uploadDocument("dm-text.csv", "text/csv");
        String documentString = extendedCcdHelper.getCcdDocumentJson("my doc text", uploadedUrl, "mydoc.txt");
        String caseId = extendedCcdHelper.createCase(documentString).getId().toString();
        extendedCcdHelper.triggerEvent(caseId, "createBundle");
        Awaitility.await().pollInterval(1, TimeUnit.SECONDS)
            .atMost(WAIT_SECONDS, TimeUnit.SECONDS).until(() -> {
                JsonNode caseJson = extendedCcdHelper.getCase(caseId);
                return !caseJson.findPath("stitchStatus").asText().equals("NEW");
            });
        JsonNode caseJson = extendedCcdHelper.getCase(caseId);
        if (caseJson.findPath("stitchStatus").asText().equals("NEW")) {
            fail("Status was not retrieved.");
        }
        assertEquals("FAILED", caseJson.findPath("stitchStatus").asText());
        assertEquals("Error converting document: my doc text with file type: text/csv",
            caseJson.findPath("stitchingFailureMessage").asText());
    }
}
