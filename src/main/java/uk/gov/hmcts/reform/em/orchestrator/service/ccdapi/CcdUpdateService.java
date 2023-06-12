package uk.gov.hmcts.reform.em.orchestrator.service.ccdapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;

@Service
public class CcdUpdateService {

    private final Logger log = LoggerFactory.getLogger(CcdUpdateService.class);
    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final CcdCallbackDtoCreator ccdCallbackDtoCreator;

    public CcdUpdateService(
            CoreCaseDataApi coreCaseDataApi,
            AuthTokenGenerator authTokenGenerator,
            CcdCallbackDtoCreator ccdCallbackDtoCreator
    ) {
        this.coreCaseDataApi = coreCaseDataApi;
        this.authTokenGenerator = authTokenGenerator;
        this.ccdCallbackDtoCreator = ccdCallbackDtoCreator;
    }

    public CcdCallbackDto startCcdEvent(String caseId, String triggerId, String jwt) {

        StartEventResponse startEventResponse =
                coreCaseDataApi.startEvent(
                        jwt,
                        authTokenGenerator.generate(),
                        caseId,
                        triggerId
                );

        return ccdCallbackDtoCreator.createDto(
                "caseBundles",
                jwt,
                startEventResponse);

    }

    public void submitCcdEvent(String caseId, String jwt, CcdCallbackDto ccdCallbackDto) {
        CaseDataContent caseDataContent = CaseDataContent.builder()
                .data(ccdCallbackDto.getCaseData())
                .event(Event.builder().id(ccdCallbackDto.getEventId()).build())
                .eventToken(ccdCallbackDto.getEventToken())
                .build();

        CaseResource caseDetails = coreCaseDataApi
                .createEvent(jwt, authTokenGenerator.generate(), caseId, caseDataContent
                );
        log.info("CCD event submitted for reference  {} ", caseDetails.getReference());
    }

    public CaseDetails getCaseDetails(String jwt, String serviceAuth,String uid, String jurisdictionId,
                                      String caseTypeId, String caseId) {
        return coreCaseDataApi.readForCaseWorker(jwt, serviceAuth, uid, jurisdictionId, caseTypeId, caseId);
    }
}
