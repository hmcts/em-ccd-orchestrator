package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackResponseDto;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(MockitoJUnitRunner.class)
public class DefaultUpdateCallerTest {

    @Mock
    private CcdCallbackDtoCreator ccdCallbackDtoCreator;

    @Mock
    private CcdCaseUpdater ccdCaseUpdater;

    @Mock
    private HttpServletRequest httpServletRequest;

    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    DefaultUpdateCaller defaultUpdateCaller;

    @Test
    public void executeUpdate() throws Exception {

        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();

        Mockito
            .when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
            .thenReturn(ccdCallbackDto);

        Mockito
            .when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class)))
            .thenReturn(objectMapper.readTree("{ \"p\" : 1 }"));

        CcdCallbackResponseDto ccdCallbackResponseDto =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);

        Assert.assertEquals(1, ccdCallbackResponseDto.getData().get("p").asInt());

    }

    @Test
    public void executeUpdateInputValidationException() {

        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();

        Mockito
                .when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
                .thenReturn(ccdCallbackDto);

        InputValidationException e = Mockito.mock(InputValidationException.class);

        Mockito
                .when(e.getViolations())
                .thenReturn(Stream.of("abc").collect(Collectors.toList()));

        Mockito
                .when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class)))
                .thenThrow(e);

        CcdCallbackResponseDto ccdCallbackResponseDto =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);

        Assert.assertEquals("abc", ccdCallbackResponseDto.getErrors().get(0));

    }

    @Test
    public void executeUpdateException() throws Exception {

        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();

        Mockito
                .when(ccdCallbackDtoCreator.createDto(Mockito.any(HttpServletRequest.class), Mockito.any(String.class)))
                .thenReturn(ccdCallbackDto);

        Mockito
                .when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class)))
                .thenThrow(new RuntimeException("x"));

        CcdCallbackResponseDto ccdCallbackResponseDto =
                defaultUpdateCaller.executeUpdate(ccdCaseUpdater, httpServletRequest);

        Assert.assertEquals("x", ccdCallbackResponseDto.getErrors().get(0));

    }

}
