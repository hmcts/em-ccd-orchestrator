package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CdamDto;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.mapper.StitchingDTOMapper;

import java.io.IOException;
import java.util.UUID;

@Service
public class AutomatedStitchingExecutor {

    private final Logger logger = LoggerFactory.getLogger(AutomatedStitchingExecutor.class);

    private final StitchingService stitchingService;
    private final StitchingDTOMapper stitchingDTOMapper;
    private final CallbackUrlCreator callbackUrlCreator;

    public static final String DEFAULT_TRIGGER_NAME  = "asyncStitchingComplete";

    public AutomatedStitchingExecutor(StitchingService stitchingService,
                                      StitchingDTOMapper stitchingDTOMapper, CallbackUrlCreator callbackUrlCreator) {
        this.stitchingService = stitchingService;
        this.stitchingDTOMapper = stitchingDTOMapper;
        this.callbackUrlCreator = callbackUrlCreator;
    }

    public long startStitching(CdamDto cdamDto, CcdBundleDTO ccdBundleDTO) {
        return startStitching(cdamDto, DEFAULT_TRIGGER_NAME, ccdBundleDTO);
    }

    public long startStitching(CdamDto cdamDto, String triggerId, CcdBundleDTO ccdBundleDTO) {

        if (StringUtils.isEmpty(ccdBundleDTO.getId())) {
            ccdBundleDTO.setId(UUID.randomUUID().toString());
        }

        final DocumentTaskDTO documentTask = new DocumentTaskDTO();
        documentTask.setBundle(stitchingDTOMapper.toStitchingDTO(ccdBundleDTO));

        CallbackDto callbackDto = new CallbackDto();
        callbackDto.setCallbackUrl(callbackUrlCreator.createCallbackUrl(
            cdamDto.getCaseId(), triggerId, ccdBundleDTO.getId()));
        documentTask.setCallback(callbackDto);
        documentTask.setJwt(cdamDto.getJwt());
        documentTask.setCaseId(cdamDto.getCaseId());
        documentTask.setCaseTypeId(cdamDto.getCaseTypeId());
        documentTask.setJurisdictionId(cdamDto.getJurisdictionId());
        documentTask.setServiceAuth(cdamDto.getServiceAuth());

        logger.debug("CallBackUrl is {}", callbackDto.getCallbackUrl());
        try {
            final DocumentTaskDTO createdDocumentTaskDTO = stitchingService.startStitchingTask(documentTask);
            ccdBundleDTO.setStitchStatus(createdDocumentTaskDTO.getTaskState().toString());
            return createdDocumentTaskDTO.getId();
        } catch (IOException e) {
            throw new StartStitchingException(String.format("Could not start stitching: %s for caseId: %s ",
                e.getMessage(), cdamDto.getCaseId()), e);
        }

    }


}
