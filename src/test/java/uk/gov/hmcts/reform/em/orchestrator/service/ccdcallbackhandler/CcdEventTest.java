package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CcdEventTest {

    @Test
    void testProperties() {
        CcdEvent ccdEvent = new CcdEvent("x");
        assertEquals("x", ccdEvent.getEventId());
        assertEquals("CcdEvent(eventId=x)", ccdEvent.toString());
    }

    @Test
    void testPropertiesNoArgConstructor() {
        CcdEvent ccdEvent = new CcdEvent();
        assertNull(ccdEvent.getEventId());
        assertEquals("CcdEvent(eventId=null)", ccdEvent.toString());
    }

}
