package uk.gov.hmcts.reform.em.orchestrator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfiguration {

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
