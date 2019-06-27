package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleStitchingService;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import javax.servlet.http.HttpServletRequest;


@Controller
public class CcdStitchBundleCallbackController {

    private final Logger log = LoggerFactory.getLogger(CcdStitchBundleCallbackController.class);

    private final CcdCallbackDtoCreator ccdCallbackDtoCreator;
    private final CcdBundleStitchingService ccdBundleStitchingService;

    public CcdStitchBundleCallbackController(CcdCallbackDtoCreator ccdCallbackDtoCreator, CcdBundleStitchingService ccdBundleStitchingService) {
        this.ccdCallbackDtoCreator = ccdCallbackDtoCreator;
        this.ccdBundleStitchingService = ccdBundleStitchingService;
    }

    @PostMapping(value = "/api/stitch-ccd-bundles",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CcdCallbackResponseDto> stitchCcdBundles(HttpServletRequest request) {
        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(request, "caseBundles");
        CcdCallbackResponseDto ccdCallbackResponseDto = new CcdCallbackResponseDto(ccdCallbackDto.getCaseData());

        try {
            ccdCallbackResponseDto.setData(ccdBundleStitchingService.updateCase(ccdCallbackDto));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ccdCallbackResponseDto.getErrors().add(e.getMessage());
        }

        log.info("JJJ - stitchBundle incoming request is ");
        try {
            log.info("JJJ - incoming case data is ");
            log.info(ccdCallbackDto.getCaseData().toString());
            log.info("JJJ - incoming ccd payload is ");
            log.info(ccdCallbackDto.getCcdPayload().toString());
        } catch (NullPointerException e) {
            log.error("JJJ - nullpointer exception");
        }
        log.info("JJJ - response data is ");
        log.info(ccdCallbackResponseDto.getData().toString());
        return ResponseEntity.ok(ccdCallbackResponseDto);
    }

}
