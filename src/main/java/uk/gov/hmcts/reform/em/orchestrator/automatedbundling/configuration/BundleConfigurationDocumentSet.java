package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

public class BundleConfigurationDocumentSet implements BundleConfigurationDocumentSelector {
    public String collection;
    public String name;
    public String url;
    public BundleConfigurationFilter filter;

    public static class BundleConfigurationFilter {
        public String property;
        public String value;
    }
}
