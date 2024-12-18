package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class CcdCaseDataContentTest {

    @Test
    void testProperties() {
        CcdCaseDataContent ccdCaseDataContent = getCcdCaseDataContent();

        assertEquals("eventx", ccdCaseDataContent.getEventId());
        assertEquals("token", ccdCaseDataContent.getToken());
        assertNull(ccdCaseDataContent.getData());
        assertNull(ccdCaseDataContent.getEventData());
        assertEquals("x", ccdCaseDataContent.getCaseReference());
        assertNull(ccdCaseDataContent.getDataClassification());
        assertEquals("draftId", ccdCaseDataContent.getDraftId());
        assertEquals(Boolean.TRUE, ccdCaseDataContent.getIgnoreWarning());
        assertEquals("x", ccdCaseDataContent.getSecurityClassification());
        assertEquals("CcdCaseDataContent(event=CcdEvent(eventId=eventx), data=null, eventData=null, "
                + "securityClassification=x, dataClassification=null, token=token,"
                + " ignoreWarning=true, draftId=draftId, "
                + "caseReference=x)", ccdCaseDataContent.toString());

    }

    private CcdCaseDataContent getCcdCaseDataContent() {
        CcdCaseDataContent ccdCaseDataContent = new CcdCaseDataContent();
        ccdCaseDataContent.setToken("token");
        CcdEvent ccdEvent = new CcdEvent("eventx");
        ccdCaseDataContent.setEvent(ccdEvent);
        ccdCaseDataContent.setData(null);
        ccdCaseDataContent.setEventData(null);
        ccdCaseDataContent.setCaseReference("x");
        ccdCaseDataContent.setDataClassification(null);
        ccdCaseDataContent.setDraftId("draftId");
        ccdCaseDataContent.setIgnoreWarning(true);
        ccdCaseDataContent.setSecurityClassification("x");
        return ccdCaseDataContent;
    }

}
