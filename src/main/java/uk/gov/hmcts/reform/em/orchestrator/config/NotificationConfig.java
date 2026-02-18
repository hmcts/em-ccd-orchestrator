package uk.gov.hmcts.reform.em.orchestrator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.service.notify.NotificationClient;

@Configuration
@Profile("!validate")
public class NotificationConfig {

    @Value("${notify.apiKey}")
    String notificationApiKey;

    @Bean
    public NotificationClient notificationClient() {
        return new NotificationClient(notificationApiKey);
    }
}
