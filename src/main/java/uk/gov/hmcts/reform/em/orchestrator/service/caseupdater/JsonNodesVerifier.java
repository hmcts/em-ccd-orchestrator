package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;

public class JsonNodesVerifier {

    private final String[] args;

    public JsonNodesVerifier(String... args) {
        if (args.length % 2 == 1) {
            throw new IllegalArgumentException("Number of String arguments must be even");
        }
        this.args = args.clone();
    }

    public boolean verify(JsonNode jsonNode) {
        boolean result = false;
        for (int i = 0; i < args.length; i += 2) {
            result = jsonNode.at(args[i]).asText().equals(args[i + 1]);
            if (result) {
                break;
            }
        }
        return result;
    }


}
