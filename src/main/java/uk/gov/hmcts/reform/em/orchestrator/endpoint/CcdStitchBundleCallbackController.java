package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleStitchingService;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@Controller
public class CcdStitchBundleCallbackController {

    private final CcdCallbackDtoCreator ccdCallbackDtoCreator;
    private final CcdBundleStitchingService ccdBundleStitchingService;

    public CcdStitchBundleCallbackController(CcdCallbackDtoCreator ccdCallbackDtoCreator, CcdBundleStitchingService ccdBundleStitchingService) {
        this.ccdCallbackDtoCreator = ccdCallbackDtoCreator;
        this.ccdBundleStitchingService = ccdBundleStitchingService;
    }

    @PostMapping(value = "/api/stitch-cdd-bundles",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CcdCallbackResponseDto> stitchCcdBundles(HttpServletRequest request) throws IOException {
        CcdCallbackDto ccdCallbackDto = ccdCallbackDtoCreator.createDto(request, "caseBundles");
        CcdCallbackResponseDto ccdCallbackResponseDto = new CcdCallbackResponseDto(ccdCallbackDto.getCaseData());
        try {
            ccdCallbackResponseDto.setData(ccdBundleStitchingService.updateCase(ccdCallbackDto));
        } catch (Exception e) {
            ccdCallbackResponseDto.getErrors().add(e.getMessage());
        }
        return ResponseEntity.ok(ccdCallbackResponseDto);
    }

}
