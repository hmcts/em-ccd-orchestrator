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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.DefaultUpdateCaller;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

@Controller
@Tag(name = "Bundle Stitching Service", description = "Endpoint to create and stitch a Bundle.")
public class NewBundleController {

    private final Logger log = LoggerFactory.getLogger(NewBundleController.class);

    private final DefaultUpdateCaller defaultUpdateCaller;
    private final AutomatedCaseUpdater automatedCaseUpdater;

    public NewBundleController(DefaultUpdateCaller defaultUpdateCaller, AutomatedCaseUpdater automatedCaseUpdater) {
        this.defaultUpdateCaller = defaultUpdateCaller;
        this.automatedCaseUpdater = automatedCaseUpdater;
    }

    @PostMapping(value = "/api/new-bundle",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Creates and Stitches a Bundle. This is Asynchronous call. This call returns the created"
            + "Bundle with out the Stitched Document URL. Stitched Document URL is updated asynchronously by the"
            + "Stitching api against the Bundle with in the case in CCD.",
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
    public ResponseEntity<CcdCallbackResponseDto> prepareNewBundle(HttpServletRequest request) {
        log.info(String.format("Received request for : %s", request.getRequestURI()));
        return defaultUpdateCaller.executeUpdate(automatedCaseUpdater, request);
    }
}
