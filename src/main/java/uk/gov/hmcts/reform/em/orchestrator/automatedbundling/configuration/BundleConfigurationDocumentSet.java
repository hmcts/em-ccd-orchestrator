package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import java.util.List;

public class BundleConfigurationDocumentSet implements BundleConfigurationDocumentSelector {
    public String property;
    public List<BundleConfigurationFilter> filter;

    public static class BundleConfigurationFilter {
        public String property;
        public String value;
    }
}
