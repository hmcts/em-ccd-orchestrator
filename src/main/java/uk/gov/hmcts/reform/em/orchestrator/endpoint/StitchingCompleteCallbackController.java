package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.em.orchestrator.service.notification.NotificationService;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.StitchingCompleteCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.StitchingCompleteCallbackService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.TaskState;
import uk.gov.hmcts.reform.em.orchestrator.util.StringUtilities;

@Controller
@ConditionalOnProperty("endpoint-toggles.stitching-complete-callback")
@Tag(name = "Stitching Callback Service", description = "Endpoint for Stitching complete callback.")
@SuppressWarnings("squid:S2139")
public class StitchingCompleteCallbackController {

    private final Logger log = LoggerFactory.getLogger(StitchingCompleteCallbackController.class);

    private final StitchingCompleteCallbackService stitchingCompleteCallbackService;
    private final NotificationService notificationService;

    @Value("${notify.successTemplateId}")
    private String successTemplateId;

    @Value("${notify.failureTemplateId}")
    private String failureTemplateId;

    public StitchingCompleteCallbackController(StitchingCompleteCallbackService stitchingCompleteCallbackService,
                                               NotificationService notificationService) {
        this.stitchingCompleteCallbackService = stitchingCompleteCallbackService;
        this.notificationService = notificationService;
    }

    @PostMapping(
        value = "/api/stitching-complete-callback/{caseId}/{triggerId}/{bundleId}",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Call back to update the Stitched Document details and the Stitched Status against the Bundle"
        + "in the case in CCD.",
        parameters = {
            @Parameter(in = ParameterIn.HEADER, name = "authorization",
                description = "Authorization (Idam Bearer token)", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.HEADER, name = "serviceauthorization",
                description = "Service Authorization (S2S Bearer token)", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.PATH, name = "caseId",
                description = "Case Id", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.PATH, name = "triggerId",
                description = "Trigger Id", required = true,
                schema = @Schema(type = "string")),
            @Parameter(in = ParameterIn.PATH, name = "bundleId",
                description = "Bundle Id", required = true,
                schema = @Schema(type = "string"))})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "403", description = "Access Denied")
    })
    public ResponseEntity<CallbackException> stitchingCompleteCallback(HttpServletRequest request,
                                                                       @PathVariable String caseId,
                                                                       @PathVariable String triggerId,
                                                                       @PathVariable String bundleId,
                                                                       @RequestBody DocumentTaskDTO documentTaskDTO) {
        String jwt = request.getHeader("authorization");
        TaskState taskState = documentTaskDTO.getTaskState();

        try {
            StitchingCompleteCallbackDto stitchingCompleteCallbackDto =
                new StitchingCompleteCallbackDto(
                    jwt,
                    caseId,
                    triggerId,
                    bundleId,
                    documentTaskDTO);

            stitchingCompleteCallbackService.handleCallback(stitchingCompleteCallbackDto);

            if (log.isInfoEnabled()) {
                log.info("Successful callback for caseId: {} and triggerId {}",
                        StringUtilities.convertValidLog(caseId),
                        StringUtilities.convertValidLog(triggerId)
                );
            }
            if ((documentTaskDTO.getBundle().getEnableEmailNotification() != null
                && documentTaskDTO.getBundle().getEnableEmailNotification())
                && (taskState.equals(TaskState.DONE) || taskState.equals(TaskState.FAILED))) {
                notificationService.sendEmailNotification(
                    taskState.equals(TaskState.DONE) ? successTemplateId : failureTemplateId,
                    jwt,
                    caseId,
                    documentTaskDTO.getBundle().getBundleTitle(),
                    taskState.equals(TaskState.DONE) ? null : documentTaskDTO.getFailureDescription()
                );
            }

            return ResponseEntity.ok().build();

        } catch (CallbackException e) {
            log.error(String.format("Unsuccessful callback: %s", e.toString()));
            return ResponseEntity.status(e.getHttpStatus()).body(e);
        }
    }
}
