package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.JsonNode;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.em.orchestrator.testutil.ExtendedCcdHelper;
import uk.gov.hmcts.reform.em.orchestrator.testutil.Pair;
import uk.gov.hmcts.reform.em.orchestrator.testutil.TestUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SecureAutomatedBundlingWithCallbacksTest extends BaseTest {

    private static final int WAIT_SECONDS = 60;
    public static final String STITCH_STATUS = "stitchStatus";

    @Autowired
    protected SecureAutomatedBundlingWithCallbacksTest(
            TestUtil testUtil,
            ExtendedCcdHelper extendedCcdHelper
    ) {
        super(testUtil, extendedCcdHelper);
    }

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

        Callable<JsonNode> callable = () -> extendedCcdHelper.getCase(caseId);
        Predicate<JsonNode> predicate = jsonNode ->
            !jsonNode.findPath(STITCH_STATUS).asText().equals("NEW");

        Awaitility.await().pollInterval(1, TimeUnit.SECONDS)
            .atMost(WAIT_SECONDS, TimeUnit.SECONDS).until(callable, predicate);

        JsonNode caseJson = callable.call();
        if (caseJson.findPath(STITCH_STATUS).asText().equals("NEW")) {
            fail("Status was not retrieved.");
        }
        assertEquals("DONE", caseJson.findPath(STITCH_STATUS).asText());
        assertEquals("null", caseJson.findPath("stitchingFailureMessage").asText());
    }

    @Test
    void testUnSuccessfulAsyncStitching() throws IOException {
        List<Pair<String, String>> fileDetails = new ArrayList<>();
        fileDetails.add(Pair.of("dm-text.csv", "text/csv"));
        String documentString = testUtil.uploadCdamDocuments(fileDetails);

        String caseId = extendedCcdHelper.createCdamCase(documentString).getId().toString();
        extendedCcdHelper.triggerEvent(caseId, "createBundle");
        Awaitility.await().pollInterval(1, TimeUnit.SECONDS)
            .atMost(WAIT_SECONDS, TimeUnit.SECONDS).until(() -> {
                JsonNode caseJson = extendedCcdHelper.getCase(caseId);
                return !caseJson.findPath(STITCH_STATUS).asText().equals("NEW");
            });
        JsonNode caseJson = extendedCcdHelper.getCase(caseId);
        if (caseJson.findPath(STITCH_STATUS).asText().equals("NEW")) {
            fail("Status was not retrieved.");
        }
        assertEquals("FAILED", caseJson.findPath(STITCH_STATUS).asText());
        assertEquals("Unknown file type: text/csv",
            caseJson.findPath("stitchingFailureMessage").asText());
    }
}
