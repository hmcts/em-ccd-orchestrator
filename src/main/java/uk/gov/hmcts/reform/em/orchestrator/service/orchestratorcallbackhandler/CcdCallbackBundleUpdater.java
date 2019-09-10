package uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;

import java.util.stream.StreamSupport;

@Service
public class CcdCallbackBundleUpdater {

    private final Logger log = LoggerFactory.getLogger(CcdCallbackBundleUpdater.class);

    private final ObjectMapper objectMapper;

    public CcdCallbackBundleUpdater(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void updateBundle(CcdCallbackDto ccdCallbackDto, StitchingCompleteCallbackDto stitchingCompleteCallbackDto)
            throws CallbackException {

        ArrayNode bundles = ccdCallbackDto
                .findCaseProperty(ArrayNode.class)
                .orElseThrow(() -> new CallbackException(400, null, "Bundle collection could not be found"));

        StreamSupport.stream(bundles.spliterator(), false)
                .filter(jsonNode ->
                        jsonNode.findValue("id").asText()
                            .equals(stitchingCompleteCallbackDto.getCcdBundleId().toString()))
                .findFirst()
                .ifPresent(ccdBundleJson -> this.updateCcdBundle(ccdBundleJson, stitchingCompleteCallbackDto));
    }

    private void updateCcdBundle(JsonNode ccdBundle, StitchingCompleteCallbackDto stitchingCompleteCallbackDto) {
        try {
            CcdBundleDTO ccdBundleDTO = this.objectMapper.treeToValue(ccdBundle, CcdBundleDTO.class);
            log.info(String.format("Updating bundle#%s with %s",
                    stitchingCompleteCallbackDto.getCcdBundleId().toString(),
                    stitchingCompleteCallbackDto.getDocumentTaskDTO().toString()));
            ccdBundleDTO.setStitchStatus(stitchingCompleteCallbackDto.getDocumentTaskDTO().getTaskState().toString());
            ccdBundleDTO.setEligibleForCloningAsBoolean(false);
            ccdBundleDTO.setStitchingFailureMessage(stitchingCompleteCallbackDto.getDocumentTaskDTO()
                    .getFailureDescription());
            ccdBundleDTO.setStitchedDocument(new CcdDocument(
                    stitchingCompleteCallbackDto.getDocumentTaskDTO().getBundle().getStitchedDocumentURI(),
                    stitchingCompleteCallbackDto.getDocumentTaskDTO().getBundle().getFileName() != null
                            ? stitchingCompleteCallbackDto.getDocumentTaskDTO().getBundle().getFileName() : "stitched.pdf",
                    stitchingCompleteCallbackDto.getDocumentTaskDTO().getBundle().getStitchedDocumentURI() + "/binary"));

            JsonNode updatedCcdBundle = objectMapper.valueToTree(ccdBundleDTO);
            ObjectNode ccdBundleObjectNode = (ObjectNode) ccdBundle;
            ccdBundleObjectNode.set("stitchStatus", updatedCcdBundle.get("stitchStatus"));
            ccdBundleObjectNode.set("eligibleForCloningAsBoolean", updatedCcdBundle.get("eligibleForCloningAsBoolean"));
            ccdBundleObjectNode.set("setStitchingFailureMessage", updatedCcdBundle.get("setStitchingFailureMessage"));
            ccdBundleObjectNode.set("stitchedDocument", updatedCcdBundle.get("stitchedDocument"));
        } catch (JsonProcessingException e) {
            throw new CallbackException(400, null, String.format("Error processing JSON %s", e.getMessage()));
        }

    }

}
