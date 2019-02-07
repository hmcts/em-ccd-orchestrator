package uk.gov.hmcts.reform.em.orchestrator.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.orchestrator.service.CasePropertyFinder;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdCallbackDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.CcdCallbackHandlerService;
import uk.gov.hmcts.reform.em.orchestrator.service.CcdCaseUpdater;

@Service
@Transactional
public class CcdCallbackHandlerServiceImpl implements CcdCallbackHandlerService {

    private CasePropertyFinder casePropertyFinder;

    public CcdCallbackHandlerServiceImpl(CasePropertyFinder casePropertyFinder) {
        this.casePropertyFinder = casePropertyFinder;
    }

    @Override
    public JsonNode handleCddCallback(final CcdCallbackDTO ccdCallbackDto, final CcdCaseUpdater ccdCaseUpdater) {
        return casePropertyFinder
            .findCaseProperty(ccdCallbackDto.getCaseData(), ccdCallbackDto.getPropertyName())
                .map( foundPropertyValue -> {
                    ccdCaseUpdater.updateCase(foundPropertyValue, ccdCallbackDto.getJwt());
                    return ccdCallbackDto.getCaseData();
                })
                .orElse(ccdCallbackDto.getCaseData());

    }
}
