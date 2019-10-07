package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.CallbackException;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.StitchingCompleteCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler.StitchingCompleteCallbackService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;


@Controller
@ConditionalOnProperty("endpoint-toggles.stitching-complete-callback")
public class StitchingCompleteCallbackController {

    private final Logger log = LoggerFactory.getLogger(StitchingCompleteCallbackController.class);

    private final StitchingCompleteCallbackService stitchingCompleteCallbackService;

    public StitchingCompleteCallbackController(StitchingCompleteCallbackService stitchingCompleteCallbackService) {
        this.stitchingCompleteCallbackService = stitchingCompleteCallbackService;
    }

    @PostMapping(value = "/api/stitching-complete-callback/{caseId}/{triggerId}/{bundleId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CallbackException> stitchingCompleteCallback(HttpServletRequest request,
                                                                       @PathVariable String caseId,
                                                                       @PathVariable String triggerId,
                                                                       @PathVariable UUID bundleId,
                                                                       @RequestBody DocumentTaskDTO documentTaskDTO) {

        try {

            StitchingCompleteCallbackDto stitchingCompleteCallbackDto =
                    new StitchingCompleteCallbackDto(request.getHeader("authorization"),
                            caseId,
                            triggerId,
                            bundleId,
                            documentTaskDTO);

            stitchingCompleteCallbackService.handleCallback(stitchingCompleteCallbackDto);

            log.error(String.format("Successful callback for caseId: %s and triggerId %s", caseId, triggerId));

            return ResponseEntity.ok().build();

        } catch (CallbackException e) {
            log.error(String.format("Unsuccessful callback: %s", e.toString()));
            return ResponseEntity.status(e.getHttpStatus()).body(e);
        }

    }

}
