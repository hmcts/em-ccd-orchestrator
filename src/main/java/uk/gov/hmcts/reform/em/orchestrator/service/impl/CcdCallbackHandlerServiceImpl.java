package uk.gov.hmcts.reform.em.orchestrator.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.orchestrator.service.CasePropertyFinder;
import uk.gov.hmcts.reform.em.orchestrator.service.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.CcdCallbackHandlerService;
import uk.gov.hmcts.reform.em.orchestrator.service.CcdCaseUpdater;

@Service
@Transactional
public class CcdCallbackHandlerServiceImpl implements CcdCallbackHandlerService {

    private CasePropertyFinder casePropertyFinder;

    public CcdCallbackHandlerServiceImpl(CasePropertyFinder casePropertyFinder) {
        this.casePropertyFinder = casePropertyFinder;
    }


    // TODO FIX THIS
    @Override
    public JsonNode handleCddCallback(final CcdCallbackDto ccdCallbackDto, final CcdCaseUpdater ccdCaseUpdater) {
        return ccdCallbackDto.getPropertyName().map( propertyName -> {
            casePropertyFinder
                    .findCaseProperty(ccdCallbackDto.getCaseData(), propertyName)
                    .map(foundPropertyValue -> {
                        ccdCaseUpdater.updateCase(ccdCallbackDto.getCaseData(), foundPropertyValue, ccdCallbackDto.getJwt());
                        return ccdCallbackDto.getCaseData();
                    })
                    .orElse(ccdCallbackDto.getCaseData());
        }).orElse(() -> {
                ccdCaseUpdater.updateCase(ccdCallbackDto.getCaseData(), null, ccdCallbackDto.getJwt());
                return ccdCallbackDto.getCaseData()
        });

    }
}
