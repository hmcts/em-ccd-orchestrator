package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Ccd Bundle Stitching Service", description = "Endpoint to stitch a Ccd Bundle.")
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
    @Operation(summary = "Creates and Stitches a Ccd Bundle. This is Synchronous call.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "authorization",
                            description = "Authorization (Idam Bearer token)", required = true,
                            schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                            description = "Service Authorization (S2S Bearer token)", required = true,
                            schema = @Schema(type = "string"))})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(schema = @Schema(implementation = CcdCallbackResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<CcdCallbackResponseDto> stitchCcdBundles(HttpServletRequest request) {
        return defaultUpdateCaller.executeUpdate(ccdBundleStitchingService, request);
    }

    @PostMapping(value = "/api/async-stitch-ccd-bundles",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Creates and Stitches a Ccd Bundle. This is Asynchronous call.",
            parameters = {
                    @Parameter(in = ParameterIn.HEADER, name = "authorization",
                            description = "Authorization (Idam Bearer token)", required = true,
                            schema = @Schema(type = "string")),
                    @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                            description = "Service Authorization (S2S Bearer token)", required = true,
                            schema = @Schema(type = "string"))})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(schema = @Schema(implementation = CcdCallbackResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<CcdCallbackResponseDto> asyncStitchCcdBundles(HttpServletRequest request) {
        return defaultUpdateCaller.executeUpdate(asyncCcdBundleStitchingService, request);
    }



}
