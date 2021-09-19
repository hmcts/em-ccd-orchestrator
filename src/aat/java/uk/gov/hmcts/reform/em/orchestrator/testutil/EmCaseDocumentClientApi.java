package uk.gov.hmcts.reform.em.orchestrator.testutil;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;

@Configuration
@FeignClient(configuration = CaseDocumentClientApi.class)
public interface EmCaseDocumentClientApi {
}
