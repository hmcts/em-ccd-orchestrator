package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

/**
 * Configuration loading strategy.
 */
public interface ConfigurationLoader {

    /**
     * Load the configuration from somewhere.
     * @return
     */
    BundleConfiguration load(String configuration);

}
