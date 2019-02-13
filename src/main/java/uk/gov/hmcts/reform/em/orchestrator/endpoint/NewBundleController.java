package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackHandlerService;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import javax.servlet.http.HttpServletRequest;

@Controller
public class NewBundleController {

    private final Logger log = LoggerFactory.getLogger(NewBundleController.class);

    private CcdCallbackHandlerService ccdCallbackHandlerService;
    private CcdCallbackDtoCreator ccdCallbackDtoCreator;

    public NewBundleController(CcdCallbackHandlerService ccdCallbackHandlerService, CcdCallbackDtoCreator ccdCallbackDtoCreator) {
        this.ccdCallbackHandlerService = ccdCallbackHandlerService;
        this.ccdCallbackDtoCreator = ccdCallbackDtoCreator;
    }

    @PostMapping(value = "/api/new-bundle",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CcdCallbackResponseDto> exampleServicePrepareNewBundle(HttpServletRequest request) {
        CcdCallbackDto dto = ccdCallbackDtoCreator.createDto(request, "caseBundles");
        CcdCallbackResponseDto ccdCallbackResponseDto = new CcdCallbackResponseDto(dto.getCaseData());
        try {
            ccdCallbackResponseDto.setData(ccdCallbackHandlerService.handleCddCallback(dto));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ccdCallbackResponseDto.getErrors().add(e.getMessage());
        }
        return ResponseEntity.ok(ccdCallbackResponseDto);
    }

}