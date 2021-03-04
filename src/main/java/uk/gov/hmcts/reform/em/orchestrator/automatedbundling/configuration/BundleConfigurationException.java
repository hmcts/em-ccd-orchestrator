package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

public class BundleConfigurationException extends RuntimeException {

    public BundleConfigurationException(String error, Exception cause) {
        super(error, cause);
    }

    public BundleConfigurationException(String error) {
        super(error);
    }
}
