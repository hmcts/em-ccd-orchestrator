package uk.gov.hmcts.reform.em.orchestrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedStitchingExecutor;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.BundleFactory;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.LocalConfigurationLoader;


@Configuration
public class AutomatedCaseConfiguration {

    @Bean
    AutomatedCaseUpdater automatedCaseUpdater(AutomatedStitchingExecutor automatedStitchingExecutor) {
        return new AutomatedCaseUpdater(
                localConfigurationLoader(),
                JacksonMapperFactory.createJsonMapper(),
                new BundleFactory(),
                automatedStitchingExecutor
        );
    }

    @Bean
    LocalConfigurationLoader localConfigurationLoader() {
        return new LocalConfigurationLoader(JacksonMapperFactory.createYamlMapper());
    }
}
