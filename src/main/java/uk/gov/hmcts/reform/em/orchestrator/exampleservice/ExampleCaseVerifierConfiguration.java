package uk.gov.hmcts.reform.em.orchestrator.exampleservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.JsonNodesVerifier;

@Configuration
public class ExampleCaseVerifierConfiguration {

    // Define this as your service's jurisdiction
    private static final String JURISDICTION = "PUBLICLAW";

    // Define this as your service's caseTypeId
    private static final String CASETYPEID = "CCD_BUNDLE_MVP_TYPE";

    @Autowired
    private ObjectMapper mapper;

    @Bean
    ExampleAddCaseBundleService exampleAddCaseBundleService() {
        return new ExampleAddCaseBundleService(
            new ExampleBundlePopulator(mapper),
            new JsonNodesVerifier(
                "/case_details/jurisdiction", JURISDICTION,
                "/case_details/case_type_id", CASETYPEID
            ),
            mapper
        );
    }

}
