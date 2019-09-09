package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.mapper.StitchingDTOMapper;

import java.io.IOException;

@Service
public class AutomatedStitchingExecutor {

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
            stitchingService.startStitchingTask(documentTask, jwt);
        } catch (IOException e) {
            throw new StartStitchingException(String.format("Could not start stitching: %s", e.getMessage()), e);
        }

    }


}
