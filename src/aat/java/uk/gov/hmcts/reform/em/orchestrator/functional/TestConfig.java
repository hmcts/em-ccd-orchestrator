package uk.gov.hmcts.reform.em.orchestrator.functional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.em.test.cdam.CdamHelper;
import uk.gov.hmcts.reform.em.test.idam.IdamHelper;
import uk.gov.hmcts.reform.em.test.s2s.S2sHelper;

@Configuration
@FeignClient(configuration = CaseDocumentClientApi.class)
@ComponentScan({ "uk.gov.hmcts.reform" })
public class TestConfig {

    private CaseDocumentClientApi caseDocumentClientApi;

    private S2sHelper xuiS2sHelper;

    private IdamHelper idamHelper;

    @Autowired
    public TestConfig(
            CaseDocumentClientApi caseDocumentClientApi,
            IdamHelper idamHelper,
            @Qualifier("xuiS2sHelper")S2sHelper xuiS2sHelper
    ) {
        this.caseDocumentClientApi = caseDocumentClientApi;
        this.idamHelper = idamHelper;
        this.xuiS2sHelper = xuiS2sHelper;
    }

    @Bean
    public CdamHelper cdamHelper() {
        return new CdamHelper(caseDocumentClientApi, xuiS2sHelper, idamHelper);
    }
}
