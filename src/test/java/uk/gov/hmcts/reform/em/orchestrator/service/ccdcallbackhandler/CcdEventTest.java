package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import org.junit.Assert;
import org.junit.Test;

public class CcdEventTest {

    @Test
    public void testProperties() {
        CcdEvent ccdEvent = new CcdEvent("x");
        Assert.assertEquals("x", ccdEvent.getEventId());
        Assert.assertEquals("CcdEvent(eventId=x)", ccdEvent.toString());
    }

    @Test
    public void testPropertiesNoArgConstructor() {
        CcdEvent ccdEvent = new CcdEvent();
        Assert.assertNull(ccdEvent.getEventId());
        Assert.assertEquals("CcdEvent(eventId=null)", ccdEvent.toString());
    }

}
