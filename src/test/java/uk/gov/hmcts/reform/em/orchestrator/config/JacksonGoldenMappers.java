package uk.gov.hmcts.reform.em.orchestrator.config;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Mappers used by Jackson golden / data-integrity tests.
 * Mirrors production owners: Boot HTTP/CCD bean vs {@link JacksonMapperFactory}.
 */
public final class JacksonGoldenMappers {

    private JacksonGoldenMappers() {
    }

    /**
     * Approximates Boot 4 {@code spring.jackson.use-jackson2-defaults=true}
     * (configureForJackson2 + Boot date/view defaults).
     */
    public static ObjectMapper bootLikeJsonMapper() {
        return JsonMapper.builder()
            .configureForJackson2()
            .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, DateTimeFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
            .build();
    }

    public static ObjectMapper factoryJsonMapper() {
        return JacksonMapperFactory.createJsonMapper();
    }
}
