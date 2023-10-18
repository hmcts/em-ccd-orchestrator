package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdBundleCloningService;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

@Controller
@Tag(name = "Clone Bundle Service", description = "Endpoint to clone an existing Bundle.")
public class CcdCloneBundleController {

    private final DefaultUpdateCaller defaultUpdateCaller;
    private final CcdBundleCloningService ccdBundleCloningService;

    public CcdCloneBundleController(DefaultUpdateCaller defaultUpdateCaller,
                                    CcdBundleCloningService ccdBundleCloningService) {
        this.defaultUpdateCaller = defaultUpdateCaller;
        this.ccdBundleCloningService = ccdBundleCloningService;
    }

    @PostMapping(value = "/api/clone-ccd-bundles",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Creates a clone of an existing Bundle.",
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
    public ResponseEntity<CcdCallbackResponseDto> cloneCcdBundles(HttpServletRequest request) {
        return defaultUpdateCaller.executeUpdate(ccdBundleCloningService, request);
    }

}
