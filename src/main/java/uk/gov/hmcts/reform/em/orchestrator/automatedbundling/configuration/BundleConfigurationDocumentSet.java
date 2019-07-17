package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class BundleConfigurationDocumentSet implements BundleConfigurationDocumentSelector {
    public final String property;
    public final List<BundleConfigurationFilter> filters;

    public static class BundleConfigurationFilter {
        public final String property;
        public final String value;

        public BundleConfigurationFilter(@JsonProperty("property") String property,
                                         @JsonProperty("value") String value) {
            this.property = property;
            this.value = value;
        }
    }

    public BundleConfigurationDocumentSet(@JsonProperty("property") String property,
                                          @JsonProperty("filters") List<BundleConfigurationFilter> filters) {
        this.property = property;
        this.filters = filters == null ? new ArrayList<>() : filters;
    }


}
