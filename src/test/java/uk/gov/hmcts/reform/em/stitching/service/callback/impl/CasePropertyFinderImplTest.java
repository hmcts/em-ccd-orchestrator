package uk.gov.hmcts.reform.em.stitching.service.callback.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CasePropertyFinderImplTest {

    @InjectMocks
    CasePropertyFinderImpl casePropertyFinder;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void findCasePropertyPropertyNotFound() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"abc\":1}");
        Assert.assertFalse(casePropertyFinder.findCaseProperty(jsonNode, "def").isPresent());
    }

    @Test
    public void findCasePropertyPropertyFound() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"abc\":1}");
        Assert.assertTrue(casePropertyFinder.findCaseProperty(jsonNode, "abc").isPresent());
    }

    @Test(expected = NullPointerException.class)
    public void findCasePropertyJsonNodeNull() {
        casePropertyFinder.findCaseProperty(null, "abc").isPresent();
    }

    @Test(expected = NullPointerException.class)
    public void findCasePropertyPropertyNameNull() throws Exception {
        JsonNode jsonNode = objectMapper.readTree("{\"abc\":1}");
        casePropertyFinder.findCaseProperty(jsonNode, null).isPresent();
    }
}