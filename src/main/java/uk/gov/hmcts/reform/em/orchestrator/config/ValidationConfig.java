package uk.gov.hmcts.reform.em.orchestrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import uk.gov.service.notify.NotificationClient;

@Configuration
@Profile("validate")
public class ValidationConfig {

    @Bean
    @Primary
    public NotificationClient notificationClient() {
        // Return a mock/dummy NotificationClient for validation profile
        // Using a dummy API key since the client won't actually be used during validation
        return new NotificationClient("dummy-validation-key");
    }
}
