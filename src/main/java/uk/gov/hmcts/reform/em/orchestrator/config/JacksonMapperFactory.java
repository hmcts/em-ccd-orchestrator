package uk.gov.hmcts.reform.em.orchestrator.config;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;


/**
 * Builds Jackson 3 mappers with Boot-3 / Jackson-2 compatible defaults used by this service.
 */
public final class JacksonMapperFactory {

    private JacksonMapperFactory() {
    }

    public static ObjectMapper createJsonMapper() {
        return JsonMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .build();
    }

    public static YAMLMapper createYamlMapper() {
        return YAMLMapper.builder()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .build();
    }
}
