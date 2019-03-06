package uk.gov.hmcts.reform.em.orchestrator.exampleservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.JsonNodesVerifier;

@Configuration
public class ExampleCaseVerifierConfiguration {

    // Define this as your service's jurisdiction
    private static final String JURISDICTION = "PUBLICLAW";

    // Define this as your service's caseTypeId
    private static final String CASETYPEID = "CCD_BUNDLE_MVP_TYPE";

    @Bean
    JsonNodesVerifier exampleCaseVerifier() {
        return new JsonNodesVerifier(
                "/case_details/jurisdiction", JURISDICTION,
                "/case_details/case_type_id", CASETYPEID);
    }

}
