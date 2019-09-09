package uk.gov.hmcts.reform.em.orchestrator.service.orchestratorcallbackhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.StitchingBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.TaskState;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CcdCallbackBundleUpdaterTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private CcdCallbackBundleUpdater ccdCallbackBundleUpdater = new CcdCallbackBundleUpdater(objectMapper);

    @Test
    public void updateBundle() throws Exception {

        JsonNode jsonNode = objectMapper.readTree("{\"caseBundles\": [{\"id\": \"922639a4-9b06-4574-b329-ce7ecf845d6b\"}]}");

        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setPropertyName(Optional.of("caseBundles"));
        ccdCallbackDto.setCaseData(jsonNode);


        DocumentTaskDTO documentTaskDTO = new DocumentTaskDTO();
        documentTaskDTO.setTaskState(TaskState.DONE);
        StitchingBundleDTO stitchingBundleDTO = new StitchingBundleDTO();
        stitchingBundleDTO.setFileName("aa.pdf");
        stitchingBundleDTO.setStitchedDocumentURI("https://aaa.com/pdf");
        documentTaskDTO.setBundle(stitchingBundleDTO);

        StitchingCompleteCallbackDto stitchingCompleteCallbackDto =
                new StitchingCompleteCallbackDto("1", "1", "1",
                        UUID.fromString("922639a4-9b06-4574-b329-ce7ecf845d6b"),
                        documentTaskDTO);

        ccdCallbackBundleUpdater.updateBundle(ccdCallbackDto, stitchingCompleteCallbackDto);

        assertEquals("DONE", ccdCallbackDto.getCaseData().findPath("caseBundles").get(0).findValue("stitchStatus").asText());
        assertEquals("aa.pdf", ccdCallbackDto.getCaseData().findPath("caseBundles").get(0).findValue("document_filename").asText());
        assertEquals("https://aaa.com/pdf", ccdCallbackDto.getCaseData().findPath("caseBundles").get(0).findValue("document_url").asText());
        assertEquals("https://aaa.com/pdf/binary", ccdCallbackDto.getCaseData().findPath("caseBundles").get(0).findValue("document_binary_url").asText());
    }

    @Test
    public void updateBundleWithException() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setCaseData(objectMapper.readTree("{\"caseBundles\": [{\"id\": \"922639a4-9b06-4574-b329-ce7ecf845d6b\"}]}"));
        assertThrows(CallbackException.class, () -> ccdCallbackBundleUpdater.updateBundle(ccdCallbackDto, null));
    }

}
