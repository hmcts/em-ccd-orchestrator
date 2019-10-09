package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.mapper.StitchingDTOMapper;

import java.io.IOException;

@Service
public class AutomatedStitchingExecutor {

    private final Logger log = LoggerFactory.getLogger(AutomatedStitchingExecutor.class);

    private final StitchingService stitchingService;
    private final StitchingDTOMapper stitchingDTOMapper;
    private final CallbackUrlCreator callbackUrlCreator;

    public AutomatedStitchingExecutor(StitchingService stitchingService, StitchingDTOMapper stitchingDTOMapper, CallbackUrlCreator callbackUrlCreator) {
        this.stitchingService = stitchingService;
        this.stitchingDTOMapper = stitchingDTOMapper;
        this.callbackUrlCreator = callbackUrlCreator;
    }

    public void startStitching(String caseId, String triggerId, String jwt, CcdBundleDTO ccdBundleDTO) {

        final DocumentTaskDTO documentTask = new DocumentTaskDTO();
        documentTask.setBundle(stitchingDTOMapper.toStitchingDTO(ccdBundleDTO));

        CallbackDto callbackDto = new CallbackDto();
        callbackDto.setCallbackUrl(callbackUrlCreator.createCallbackUrl(caseId, triggerId, ccdBundleDTO.getId()));
        documentTask.setCallback(callbackDto);

        try {
            log.info("Starting new stitching task {}", documentTask.toString());
            stitchingService.startStitchingTask(documentTask, jwt);
        } catch (IOException e) {
            log.error(String.format("Starting new stitching task - failed %s", documentTask.toString()), e);
            throw new StartStitchingException(String.format("Could not start stitching: %s", e.getMessage()), e);
        }

    }


}
