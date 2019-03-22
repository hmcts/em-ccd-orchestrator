package uk.gov.hmcts.reform.em.orchestrator.financialremedyservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.JsonNodesVerifier;

/**
 * JURISDICTION: Define this as your service's jurisdiction.
 * CASE_TYPE_ID : Define this as your service's caseTypeId.
 */
@Configuration
public class FinancialRemedyCaseVerifierConfiguration {

    private static final String JURISDICTION = "DIVORCE";
    private static final String CASE_TYPE_ID = "FinancialRemedyContested";

    @Autowired
    private ObjectMapper mapper;

    @Bean
    public FinancialRemedyAddCaseBundleService financialRemedyAddCaseBundleService() {
        return new FinancialRemedyAddCaseBundleService(
            new FinancialRemedyBundlePopulator(mapper),
            new JsonNodesVerifier(
                "/case_details/jurisdiction", JURISDICTION,
                "/case_details/case_type_id", CASE_TYPE_ID
            ),
            mapper
        );
    }
}
