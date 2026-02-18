package uk.gov.hmcts.reform.em.orchestrator.config;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.mapper.StitchingDTOMapper;

@Configuration
public class Config {


    @Value("${em-rpa-stitching-api.base-url}")
    private String stitchingBaseUrl;

    @Value("${em-rpa-stitching-api.resource}")
    private String stitchingResource;

    @Value("${max-retry-to-poll-stitching}")
    int maxRetryToPollStitching;

    @Bean
    public StitchingService getStitchingService(AuthTokenGenerator authTokenGenerator, OkHttpClient http) {
        return new StitchingService(
                new StitchingDTOMapper(),
                http,
                stitchingBaseUrl + stitchingResource,
                authTokenGenerator,
                maxRetryToPollStitching
        );
    }

}
