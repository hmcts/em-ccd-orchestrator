package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodesVerifier {

    private final String[] args;

    public JsonNodesVerifier(String... args) {
        if (args.length < 2 || args.length % 2 == 1) {
            throw new IllegalArgumentException("Number of String arguments must be even");
        }
        this.args = args.clone();
    }

    public boolean verify(JsonNode jsonNode) {
        for (int i = 0; i < args.length; i += 2) {
            if (!jsonNode.at(args[i]).asText().equals(args[i + 1])) {
                return false;
            }
        }
        return true;
    }


}
