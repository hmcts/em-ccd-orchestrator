package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class JsonNodesVerifierTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenLessThan2Args() {
        new JsonNodesVerifier("x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenOddNumberOfArgs() {
        new JsonNodesVerifier("x","y","z");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenNpArgs() {
        new JsonNodesVerifier();
    }

    @Test
    public void verifyOK() throws Exception {
        JsonNodesVerifier jsonNodesVerifier = new JsonNodesVerifier("/x", "y");

        assertTrue(jsonNodesVerifier.verify(objectMapper.readTree("{\"x\":\"y\"}")));
    }

    @Test
    public void verifyMultipleProperties() throws Exception {
        JsonNodesVerifier jsonNodesVerifier = new JsonNodesVerifier(
            "/a", "b",
            "/x", "y"
        );

        assertTrue(jsonNodesVerifier.verify(objectMapper.readTree("{\"a\":\"b\",\"x\":\"y\"}")));
    }

    @Test
    public void verifyNotMatch() throws Exception {
        JsonNodesVerifier jsonNodesVerifier = new JsonNodesVerifier(
            "/a", "b",
            "/x", "z"
        );

        assertFalse(jsonNodesVerifier.verify(objectMapper.readTree("{\"a\":\"b\",\"x\":\"y\"}")));
    }
}
