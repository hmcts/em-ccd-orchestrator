package uk.gov.hmcts.reform.em.stitching.service.callback.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.em.stitching.service.callback.CasePropertyFinder;
import uk.gov.hmcts.reform.em.stitching.service.callback.CcdCallbackDto;
import uk.gov.hmcts.reform.em.stitching.service.callback.CcdCallbackHandlerService;
import uk.gov.hmcts.reform.em.stitching.service.callback.CcdCaseUpdater;

@Service
@Transactional
public class CcdCallbackHandlerServiceImpl implements CcdCallbackHandlerService {

    private CasePropertyFinder casePropertyFinder;

    public CcdCallbackHandlerServiceImpl(CasePropertyFinder casePropertyFinder) {
        this.casePropertyFinder = casePropertyFinder;
    }

    @Override
    public JsonNode handleCddCallback(final CcdCallbackDto ccdCallbackDto, final CcdCaseUpdater ccdCaseUpdater) {
        return casePropertyFinder
            .findCaseProperty(ccdCallbackDto.getCaseData(), ccdCallbackDto.getPropertyName())
                .map( foundPropertyValue -> {
                    ccdCaseUpdater.updateCase(foundPropertyValue, ccdCallbackDto.getJwt());
                    return ccdCallbackDto.getCaseData();
                })
                .orElse(ccdCallbackDto.getCaseData());

    }
}
