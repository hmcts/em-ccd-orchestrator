package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class JsonNodesVerifierTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenOddNumberOfArgs() {
        new JsonNodesVerifier("x");
    }

    @Test
    public void verifyOK() throws Exception{
        JsonNodesVerifier jsonNodesVerifier = new JsonNodesVerifier("/x", "y");
        jsonNodesVerifier.verify(objectMapper.readTree("{\"x\":\"y\"}"));
    }
}