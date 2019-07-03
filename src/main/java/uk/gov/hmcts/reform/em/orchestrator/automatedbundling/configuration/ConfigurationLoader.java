package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import java.util.Optional;

/**
 * Configuration loading strategy.
 */
public interface ConfigurationLoader {

    /**
     * Load the configuration from somewhere
     */
    Optional<BundleConfiguration> load(String configuration);

}
