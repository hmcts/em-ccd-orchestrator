package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.AsyncCcdBundleStitchingService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleStitchingService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CcdStitchBundleCallbackController {

    private final DefaultUpdateCaller defaultUpdateCaller;
    private final AsyncCcdBundleStitchingService asyncCcdBundleStitchingService;
    private final CcdBundleStitchingService ccdBundleStitchingService;

    public CcdStitchBundleCallbackController(DefaultUpdateCaller defaultUpdateCaller,
                                             AsyncCcdBundleStitchingService asyncCcdBundleStitchingService,
                                             CcdBundleStitchingService ccdBundleStitchingService) {
        this.defaultUpdateCaller = defaultUpdateCaller;
        this.asyncCcdBundleStitchingService = asyncCcdBundleStitchingService;
        this.ccdBundleStitchingService = ccdBundleStitchingService;
    }

    @PostMapping(value = "/api/stitch-ccd-bundles",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CcdCallbackResponseDto> stitchCcdBundles(HttpServletRequest request) {
        return ResponseEntity.ok(defaultUpdateCaller.executeUpdate(ccdBundleStitchingService, request));
    }

    @PostMapping(value = "/api/async-stitch-ccd-bundles",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CcdCallbackResponseDto> asyncStitchCcdBundles(HttpServletRequest request) {
        return ResponseEntity.ok(defaultUpdateCaller.executeUpdate(asyncCcdBundleStitchingService, request));
    }



}
