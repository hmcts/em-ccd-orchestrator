package uk.gov.hmcts.reform.em.stitching.config;

import okhttp3.OkHttpClient;
import okhttp3.mock.MockInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientConfiguration {

    @Bean
    public OkHttpClient okHttpClient(){
        MockInterceptor mockInterceptor = new MockInterceptor();
        return new OkHttpClient.Builder().addInterceptor(mockInterceptor).build();
    }

}
