package uk.gov.hmcts.reform.em.orchestrator.endpoint;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.em.orchestrator.service.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.CcdCallbackHandlerService;
import uk.gov.hmcts.reform.em.orchestrator.service.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.addCaseBundleService;

import java.util.Optional;

@Controller
public class AppendDocumentsController {

    private final Logger log = LoggerFactory.getLogger(AppendDocumentsController.class);

    private addCaseBundleService bundleCreationService;
    private CcdCallbackHandlerService ccdCallbackHandlerService;

    public AppendDocumentsController(CcdCallbackHandlerService ccdCallbackHandlerService, addCaseBundleService bundleCreationService) {
        this.bundleCreationService = bundleCreationService;
        this.ccdCallbackHandlerService = ccdCallbackHandlerService;
    }

    private CcdCallbackDto prepareNewBundle(
            JsonNode caseData,
            String authorisationHeader,
            CcdCaseUpdater bundleCreationService) {

        CcdCallbackDto cmd = new CcdCallbackDto();

        cmd.setCaseData(caseData);

        log.debug("CCD callback request received {}", cmd.getCaseData());

        cmd.setJwt(authorisationHeader);

        cmd.setPropertyName(Optional.of("caseBundles"));

        ccdCallbackHandlerService.handleCddCallback(cmd, bundleCreationService);

        return cmd;
    }

    @PostMapping(value = "/api/exampleservice/new-bundle",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JsonNode> exampleServicePrepareNewBundle(
            @RequestBody JsonNode caseData,
            @RequestHeader(value = "Authorization", required = false) String authorisationHeader,
            CcdCaseUpdater bundleCreationService) {

        CcdCallbackDto cmd = prepareNewBundle(caseData, authorisationHeader, bundleCreationService);

        return ResponseEntity.ok(cmd.getCaseData());
    }

}