package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.JsonNode;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.em.orchestrator.testutil.Pair;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SecureAutomatedBundlingWithCallbacksTest extends BaseTest {

    private static final int WAIT_SECONDS = 60;

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @BeforeEach
    public void setUp() {
        assumeTrue(enableCdamValidation);
    }

    @Test
    void testSuccessfulAsyncStitching() throws Exception {
        List<Pair<String, String>> fileDetails = new ArrayList<>();
        fileDetails.add(Pair.of("annotationTemplate.pdf", "application/pdf"));
        String documentString = testUtil.uploadCdamDocuments(fileDetails);

        String caseId = extendedCcdHelper.createCdamCase(documentString).getId().toString();
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
        assertEquals("DONE", caseJson.findPath("stitchStatus").asText());
        assertEquals("null", caseJson.findPath("stitchingFailureMessage").asText());
    }

    @Test
    void testUnSuccessfulAsyncStitching() throws Exception {
        List<Pair<String, String>> fileDetails = new ArrayList<>();
        fileDetails.add(Pair.of("dm-text.csv", "text/csv"));
        String documentString = testUtil.uploadCdamDocuments(fileDetails);

        String caseId = extendedCcdHelper.createCdamCase(documentString).getId().toString();
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
        assertEquals("Unknown file type: text/csv",
            caseJson.findPath("stitchingFailureMessage").asText());
    }
}
