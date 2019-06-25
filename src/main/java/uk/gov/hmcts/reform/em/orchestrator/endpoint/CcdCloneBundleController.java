package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleCloningService;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import javax.servlet.http.HttpServletRequest;


@Controller
public class CcdCloneBundleController {

    private final Logger log = LoggerFactory.getLogger(CcdCloneBundleController.class);

    private final CcdCallbackDtoCreator ccdCallbackDtoCreator;
    private final CcdBundleCloningService ccdBundleCloningService;

    public CcdCloneBundleController(CcdCallbackDtoCreator ccdCallbackDtoCreator, CcdBundleCloningService ccdBundleCloningService) {
        this.ccdCallbackDtoCreator = ccdCallbackDtoCreator;
        this.ccdBundleCloningService = ccdBundleCloningService;
    }

    @PostMapping(value = "/api/clone-ccd-bundles",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CcdCallbackResponseDto> cloneCcdBundles(HttpServletRequest request) {
        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(request, "caseBundles");
        CcdCallbackResponseDto ccdCallbackResponseDto = new CcdCallbackResponseDto(ccdCallbackDto.getCaseData());

        try {
            ccdCallbackResponseDto.setData(ccdBundleCloningService.updateCase(ccdCallbackDto));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ccdCallbackResponseDto.getErrors().add(e.getMessage());
        }

        return ResponseEntity.ok(ccdCallbackResponseDto);
    }

}
