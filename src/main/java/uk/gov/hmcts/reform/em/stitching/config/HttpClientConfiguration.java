package uk.gov.hmcts.reform.em.stitching.config;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfiguration {

    @Bean
    public OkHttpClient okHttpClient(){
        return new OkHttpClient();
    }

}
