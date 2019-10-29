package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleCloningService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CcdCloneBundleController {

    private final DefaultUpdateCaller defaultUpdateCaller;
    private final CcdBundleCloningService ccdBundleCloningService;

    public CcdCloneBundleController(DefaultUpdateCaller defaultUpdateCaller, CcdBundleCloningService ccdBundleCloningService) {
        this.defaultUpdateCaller = defaultUpdateCaller;
        this.ccdBundleCloningService = ccdBundleCloningService;
    }

    @PostMapping(value = "/api/clone-ccd-bundles",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CcdCallbackResponseDto> cloneCcdBundles(HttpServletRequest request) {
        return ResponseEntity.ok(defaultUpdateCaller.executeUpdate(ccdBundleCloningService, request));
    }

}
