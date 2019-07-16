package uk.gov.hmcts.reform.em.orchestrator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.BundleFactory;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.LocalConfigurationLoader;

@Configuration
public class AutomatedCaseConfiguration {
    @Bean
    AutomatedCaseUpdater automatedCaseUpdater() {
        return new AutomatedCaseUpdater(
            new LocalConfigurationLoader(
                new ObjectMapper(
                    new YAMLFactory()
                )
            ),
            new ObjectMapper(),
            new BundleFactory()
        );
    }
}
