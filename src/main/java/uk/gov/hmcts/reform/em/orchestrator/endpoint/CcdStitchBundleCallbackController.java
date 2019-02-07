package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.em.orchestrator.service.impl.CcdBundleStitchingService;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdCallbackDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.CcdCallbackHandlerService;


@Controller
public class CcdStitchBundleCallbackController {

    private final Logger log = LoggerFactory.getLogger(CcdStitchBundleCallbackController.class);

    private CcdCallbackHandlerService ccdCallbackHandlerService;

    private CcdBundleStitchingService ccdBundleStitchingService;


    public CcdStitchBundleCallbackController(CcdCallbackHandlerService ccdCallbackHandlerService, CcdBundleStitchingService ccdBundleStitchingService) {
        this.ccdCallbackHandlerService = ccdCallbackHandlerService;
        this.ccdBundleStitchingService = ccdBundleStitchingService;
    }

    @PostMapping(value = "/api/stitch-cdd-bundles",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> stitchCcdBundles(
            @RequestBody JsonNode caseJson,
            @RequestHeader(value="Authorization", required=false) String authorisationHeader)   {

        JsonNode caseData = caseJson.path("case_details").path("case_data");

        log.debug("CCD callback request received {}", caseData);

        CcdCallbackDTO ccdCallback = new CcdCallbackDTO();
        ccdCallback.setCaseData(caseData);
        ccdCallback.setJwt(authorisationHeader);
        ccdCallback.setPropertyName("caseBundles");

        ccdCallbackHandlerService.handleCddCallback(ccdCallback, ccdBundleStitchingService);

        return ResponseEntity.ok(caseJson);
    }

}
