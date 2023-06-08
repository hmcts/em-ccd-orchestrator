package uk.gov.hmcts.reform.em.orchestrator.functional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.em.test.cdam.CdamHelper;
import uk.gov.hmcts.reform.em.test.idam.IdamHelper;
import uk.gov.hmcts.reform.em.test.s2s.S2sHelper;

@Configuration
@FeignClient(configuration = CaseDocumentClientApi.class)
@ComponentScan({ "uk.gov.hmcts.reform" })
public class TestConfig {

    @Autowired
    private CaseDocumentClientApi caseDocumentClientApi;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    @Qualifier("xuiS2sHelper")
    private S2sHelper xuiS2sHelper;

    @Autowired
    private IdamHelper idamHelper;

    @Bean
    public CdamHelper cdamHelper() {
        return new CdamHelper(caseDocumentClientApi, xuiS2sHelper, idamHelper);
    }
}
