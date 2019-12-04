package uk.gov.hmcts.reform.em.orchestrator.functional;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;

import static uk.gov.hmcts.reform.em.orchestrator.functional.TestSuiteInit.*;

public class AutomatedBundlingWithCallbacks {

    private static final int WAIT_SECONDS = 60;

    @Test
    public void testSuccessfulAsyncStitching() throws Exception {
        String uploadedUrl = testUtil.uploadDocument();
        String documentString = testUtil.getCcdHelper().getCcdDocumentJson("my doc", uploadedUrl, "mypdf.pdf");
        String caseId = testUtil.getCcdHelper().createCase(documentString);
        JsonNode createTriggerResponse = testUtil.getCcdHelper().startCaseEventAndGetToken(caseId, "createBundle");
        System.out.println(createTriggerResponse.toString());
        JsonNode finishEventResponse = testUtil.getCcdHelper().finishCaseEvent(
                caseId,
                "createBundle",
                createTriggerResponse.get("token").asText(),
                createTriggerResponse.get("case_details").get("case_data"));
        System.out.println(finishEventResponse.toString());
        int i = 0;
        while (i < WAIT_SECONDS) {
            JsonNode caseJson = testUtil.getCcdHelper().getCase(caseId);
            if (!caseJson.findPath("stitchStatus").asText().equals("NEW")) {
                Assert.assertEquals("DONE", caseJson.findPath("stitchStatus").asText());
                Assert.assertEquals("null", caseJson.findPath("stitchingFailureMessage").asText());
                break;
            }
            Thread.sleep(1000);
            System.out.println("waiting");
            i++;
        }
        if (i >= WAIT_SECONDS) {
            Assert.fail("Status was not retrieved.");
        }
    }

    @Test
    public void testUnSuccessfulAsyncStitching() throws Exception {
        String uploadedUrl = testUtil.uploadDocument("dm-text.txt", "text/plain");
        String documentString = testUtil.getCcdHelper().getCcdDocumentJson("my doc text", uploadedUrl, "mydoc.txt");
        String caseId = testUtil.getCcdHelper().createCase(documentString);
        JsonNode createTriggerResponse = testUtil.getCcdHelper().startCaseEventAndGetToken(caseId, "createBundle");
        System.out.println(createTriggerResponse.toString());
        JsonNode finishEventResponse = testUtil.getCcdHelper().finishCaseEvent(
                caseId,
                "createBundle",
                createTriggerResponse.get("token").asText(),
                createTriggerResponse.get("case_details").get("case_data"));
        System.out.println(finishEventResponse.toString());
        int i = 0;
        while (i < WAIT_SECONDS) {
            JsonNode caseJson = testUtil.getCcdHelper().getCase(caseId);
            if (!caseJson.findPath("stitchStatus").asText().equals("NEW")) {
                Assert.assertEquals("FAILED", caseJson.findPath("stitchStatus").asText());
                Assert.assertEquals("Unknown file type: text/plain", caseJson.findPath("stitchingFailureMessage").asText());
                break;
            }
            Thread.sleep(1000);
            i++;
            System.out.println("waiting");
        }
        if (i >= WAIT_SECONDS) {
            Assert.fail("Status was not retrieved.");
        }
    }
}
