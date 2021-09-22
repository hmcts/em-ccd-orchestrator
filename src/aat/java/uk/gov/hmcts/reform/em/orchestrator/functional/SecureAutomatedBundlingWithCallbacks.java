package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import uk.gov.hmcts.reform.em.test.retry.RetryRule;

import java.util.ArrayList;
import java.util.List;

public class SecureAutomatedBundlingWithCallbacks extends BaseTest {

    private final Logger logger = LoggerFactory.getLogger(SecureAutomatedBundlingWithCallbacks.class);

    private static final int WAIT_SECONDS = 60;

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Test
    public void testSuccessfulAsyncStitching() throws Exception {
        List<Pair<String, String>> fileDetails = new ArrayList<>();
        fileDetails.add(Pair.of("annotationTemplate.pdf", "application/pdf"));
        String documentString = testUtil.uploadCdamDocuments(fileDetails);

        String caseId = extendedCcdHelper.createCdamCase(documentString).getId().toString();
        extendedCcdHelper.triggerEvent(caseId, "createBundle");
        int i = 0;
        while (i < WAIT_SECONDS) {
            JsonNode caseJson = extendedCcdHelper.getCase(caseId);
            if (!caseJson.findPath("stitchStatus").asText().equals("NEW")) {
                Assert.assertEquals("DONE", caseJson.findPath("stitchStatus").asText());
                Assert.assertEquals("null", caseJson.findPath("stitchingFailureMessage").asText());
                break;
            }
            Thread.sleep(1000);
            logger.info("waiting");
            i++;
        }
        if (i >= WAIT_SECONDS) {
            Assert.fail("Status was not retrieved.");
        }
    }

    @Test
    public void testUnSuccessfulAsyncStitching() throws Exception {
        List<Pair<String, String>> fileDetails = new ArrayList<>();
        fileDetails.add(Pair.of("dm-text.csv", "text/csv"));
        String documentString = testUtil.uploadCdamDocuments(fileDetails);

        String caseId = extendedCcdHelper.createCdamCase(documentString).getId().toString();
        extendedCcdHelper.triggerEvent(caseId, "createBundle");
        int i = 0;
        while (i < WAIT_SECONDS) {
            JsonNode caseJson = extendedCcdHelper.getCase(caseId);
            logger.info(String.format("Testing %s - %s", caseId, caseJson.toPrettyString()));
            if (!caseJson.findPath("stitchStatus").asText().equals("NEW")) {
                Assert.assertEquals("FAILED", caseJson.findPath("stitchStatus").asText());
                Assert.assertEquals("Unknown file type: text/csv", caseJson.findPath("stitchingFailureMessage").asText());
                break;
            }
            Thread.sleep(1000);
            i++;
            logger.info("waiting");
        }
        if (i >= WAIT_SECONDS) {
            Assert.fail("Status was not retrieved.");
        }
    }
}
