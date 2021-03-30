package uk.gov.hmcts.reform.em.orchestrator.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedStitchingExecutor;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.BundleFactory;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.LocalConfigurationLoader;

import javax.validation.Validator;

@Configuration
public class AutomatedCaseConfiguration {

    @Autowired
    AutomatedStitchingExecutor automatedStitchingExecutor;

    @Autowired
    Validator validator;

    @Bean
    AutomatedCaseUpdater automatedCaseUpdater() {
        return new AutomatedCaseUpdater(
            localConfigurationLoader(),
            new ObjectMapper(),
            new BundleFactory(),
            automatedStitchingExecutor,
            validator
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
