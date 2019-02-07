package uk.gov.hmcts.reform.em.orchestrator.config;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.mapper.StitchingDTOMapper;

@Configuration
public class Config {

    @Autowired
    private OkHttpClient http;

    @Value("${em-rpa-stitching-api.base-url}")
    private String stitchingBaseUrl;

    @Value("${em-rpa-stitching-api.resource}")
    private String stitchingResource;

    @Bean
    public StitchingService getStitchingService() {
        return new StitchingService(new StitchingDTOMapper(), http, stitchingBaseUrl + stitchingResource);
    }
}
