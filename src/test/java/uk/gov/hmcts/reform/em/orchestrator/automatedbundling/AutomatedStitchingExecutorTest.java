package uk.gov.hmcts.reform.em.orchestrator.automatedbundling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CdamDto;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;
import uk.gov.hmcts.reform.em.orchestrator.stitching.mapper.StitchingDTOMapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AutomatedStitchingExecutorTest {

    @Mock
    private StitchingService stitchingService;
    @Mock
    private StitchingDTOMapper stitchingDTOMapper;
    @Mock
    private CallbackUrlCreator callbackUrlCreator;

    private final CdamDto cdamDto = CdamDto.builder()
            .jwt("jwt").caseId("123")
            .build();

    @InjectMocks
    private AutomatedStitchingExecutor automatedStitchingExecutor;

    @Test
    void startStitching() throws Exception {
        CcdBundleDTO ccdBundleDTO = new CcdBundleDTO();

        DocumentTaskDTO documentTaskDTO = new DocumentTaskDTO();
        documentTaskDTO.setId(575645345L);

        Mockito.when(stitchingService.startStitchingTask(Mockito.any()))
                .thenReturn(documentTaskDTO);

        automatedStitchingExecutor.startStitching(cdamDto, ccdBundleDTO);

        Mockito.verify(stitchingDTOMapper, Mockito.times(1))
                .toStitchingDTO(Mockito.any(CcdBundleDTO.class));

        Mockito.verify(stitchingService, Mockito.times(1))
                .startStitchingTask(Mockito.any(DocumentTaskDTO.class));

        Mockito.verify(callbackUrlCreator, Mockito.times(1))
                .createCallbackUrl(Mockito.anyString(), Mockito.anyString(),Mockito.any());

    }


    @Test
    void startStitchingIOException() throws Exception {
        CcdBundleDTO ccdBundleDTO = new CcdBundleDTO();

        Mockito.when(stitchingService.startStitchingTask(Mockito.any()))
                .thenThrow(new IOException("x"));

        assertThrows(StartStitchingException.class, () ->
            automatedStitchingExecutor.startStitching(cdamDto, "1234", ccdBundleDTO)
        );

        Mockito.verify(stitchingDTOMapper, Mockito.times(1))
                .toStitchingDTO(Mockito.any(CcdBundleDTO.class));

        Mockito.verify(stitchingService, Mockito.times(1))
                .startStitchingTask(Mockito.any(DocumentTaskDTO.class));

        Mockito.verify(callbackUrlCreator, Mockito.times(1))
                .createCallbackUrl(Mockito.anyString(), Mockito.anyString(),Mockito.any());

    }
}
