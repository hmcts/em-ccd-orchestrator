package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.LocalConfigurationLoader;

@Configuration
public class AutomatedCaseConfiguration {
    @Bean
    AutomatedCaseUpdater automatedCaseUpdater() {
        return new AutomatedCaseUpdater(
            localConfigurationLoader(),
            new ObjectMapper()
        );
    }

    @Bean
    LocalConfigurationLoader localConfigurationLoader() {
        return new LocalConfigurationLoader(
            new ObjectMapper(
                new YAMLFactory()
            )
        );
    }
}
