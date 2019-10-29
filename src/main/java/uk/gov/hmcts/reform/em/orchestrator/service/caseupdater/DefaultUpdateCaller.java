package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import javax.servlet.http.HttpServletRequest;

@Service
public class DefaultUpdateCaller {

    private final Logger log = LoggerFactory.getLogger(DefaultUpdateCaller.class);

    private final CcdCallbackDtoCreator ccdCallbackDtoCreator;

    public DefaultUpdateCaller(CcdCallbackDtoCreator ccdCallbackDtoCreator) {
        this.ccdCallbackDtoCreator = ccdCallbackDtoCreator;
    }

    public CcdCallbackResponseDto executeUpdate(CcdCaseUpdater ccdCaseUpdater, HttpServletRequest request) {
        CcdCallbackDto dto = ccdCallbackDtoCreator.createDto(request, "caseBundles");
        CcdCallbackResponseDto ccdCallbackResponseDto = new CcdCallbackResponseDto(dto.getCaseData());
        try {
            ccdCallbackResponseDto.setData(ccdCaseUpdater.updateCase(dto));
        } catch (InputValidationException e) {
            log.error(e.getMessage(), e);
            ccdCallbackResponseDto.getErrors().addAll(e.getViolations());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ccdCallbackResponseDto.getErrors().add(e.getMessage());
        }
        return ccdCallbackResponseDto;
    }

}
