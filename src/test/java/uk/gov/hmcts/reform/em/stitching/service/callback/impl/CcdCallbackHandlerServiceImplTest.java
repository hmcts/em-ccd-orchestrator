package uk.gov.hmcts.reform.em.stitching.service.callback.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.stitching.service.callback.CasePropertyFinder;
import uk.gov.hmcts.reform.em.stitching.service.callback.CcdCallbackDto;
import uk.gov.hmcts.reform.em.stitching.service.callback.CcdCaseUpdater;

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
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
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
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
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