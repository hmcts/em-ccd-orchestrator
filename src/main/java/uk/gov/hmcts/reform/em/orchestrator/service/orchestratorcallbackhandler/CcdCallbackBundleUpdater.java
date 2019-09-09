package uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;

import java.io.IOException;

@Service
class CcdCallbackBundleUpdater {

    private final Logger log = LoggerFactory.getLogger(CcdCallbackBundleUpdater.class);

    private final ObjectMapper objectMapper;

    CcdCallbackBundleUpdater(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    void updateBundle(CcdCallbackDto ccdCallbackDto, StitchingCompleteCallbackDto stitchingCompleteCallbackDto)
            throws CallbackException, IOException {
        ArrayNode bundles = ccdCallbackDto
                .findCaseProperty(ArrayNode.class)
                .orElseThrow(() -> new CallbackException(400, null, "Bundle collection could not be found"));

        for (int i = 0; i < bundles.size(); i++) {
            JsonNode tempBundle = bundles.get(i);
            String id = tempBundle.findValue("id").asText();
            if (stitchingCompleteCallbackDto.getCcdBundleId().toString().equals(id)) {
                CcdBundleDTO ccdBundleDTO = objectMapper.treeToValue(tempBundle, CcdBundleDTO.class);
                log.info(String.format("Updating bundle#%s with %s",
                        stitchingCompleteCallbackDto.getCcdBundleId().toString(),
                        stitchingCompleteCallbackDto.getDocumentTaskDTO().toString()));
                ccdBundleDTO.setStitchStatus(stitchingCompleteCallbackDto.getDocumentTaskDTO().getTaskState().toString());
                ccdBundleDTO.setEligibleForCloningAsBoolean(false);
                ccdBundleDTO.setStitchedDocument(new CcdDocument(
                        stitchingCompleteCallbackDto.getDocumentTaskDTO().getBundle().getStitchedDocumentURI(),
                        stitchingCompleteCallbackDto.getDocumentTaskDTO().getBundle().getFileName() != null ?
                                stitchingCompleteCallbackDto.getDocumentTaskDTO().getBundle().getFileName() : "stitched.pdf",
                        stitchingCompleteCallbackDto.getDocumentTaskDTO().getBundle().getStitchedDocumentURI() + "/binary"));
                bundles.set(i, objectMapper.valueToTree(ccdBundleDTO));
            }
            break;
        }

    }

}
