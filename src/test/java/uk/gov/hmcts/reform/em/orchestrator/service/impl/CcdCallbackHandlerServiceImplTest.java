package uk.gov.hmcts.reform.em.orchestrator.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.service.CasePropertyFinder;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdCallbackDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.CcdCaseUpdater;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class CcdCallbackHandlerServiceImplTest {

    @Mock
    CasePropertyFinder casePropertyFinder;

    @Mock
    CcdCaseUpdater ccdCaseUpdater;

    @InjectMocks
    CcdCallbackHandlerServiceImpl ccdCallbackHandlerService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testHandleSubPropertyFound() throws Exception {
        CcdCallbackDTO ccdCallbackDto = new CcdCallbackDTO();
        ccdCallbackDto.setCaseData(objectMapper.readTree("[]"));
        ccdCallbackDto.setJwt("jwt");
        ccdCallbackDto.setPropertyName("bundles");

        Mockito.when(
                casePropertyFinder
                    .findCaseProperty(Mockito.any(JsonNode.class), Mockito.any(String.class)))
                    .thenReturn(Optional.of(objectMapper.readTree("[]")));

        ccdCallbackHandlerService.handleCddCallback(ccdCallbackDto, ccdCaseUpdater);

        Mockito.verify(ccdCaseUpdater, Mockito.times(1))
                .updateCase(Mockito.any(JsonNode.class), Mockito.any(String.class));
    }

    @Test
    public void testHandleSubPropertyNotFound() throws Exception {
        CcdCallbackDTO ccdCallbackDto = new CcdCallbackDTO();
        ccdCallbackDto.setCaseData(objectMapper.readTree("[]"));
        ccdCallbackDto.setJwt("jwt");
        ccdCallbackDto.setPropertyName("bundles");

        Mockito.when(
                casePropertyFinder
                        .findCaseProperty(Mockito.any(JsonNode.class), Mockito.any(String.class)))
                .thenReturn(Optional.ofNullable(null));

        ccdCallbackHandlerService.handleCddCallback(ccdCallbackDto, ccdCaseUpdater);

        Mockito.verify(ccdCaseUpdater, Mockito.times(0))
                .updateCase(Mockito.any(JsonNode.class), Mockito.any(String.class));
    }


}