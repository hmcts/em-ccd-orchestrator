package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdater;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdaterFinder;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class CcdCallbackHandlerServiceImplTest {

    @Mock
    CcdCaseUpdaterFinder ccdCaseUpdaterFinder;

    @InjectMocks
    CcdCallbackHandlerServiceImpl ccdCallbackHandlerService;

    @Test(expected = CaseUpdaterDoesNotExistException.class)
    public void handleCddCallback() {
        Mockito.when(ccdCaseUpdaterFinder.find(Mockito.any(CcdCallbackDto.class))).thenReturn(Optional.empty());
        ccdCallbackHandlerService.handleCddCallback(new CcdCallbackDto());
    }

    @Test
    public void handleCddCallbackHandlerFound() {
        CcdCaseUpdater ccdCaseUpdater = Mockito.mock(CcdCaseUpdater.class);
        JsonNode jsonNode = Mockito.mock(JsonNode.class);
        Mockito.when(ccdCaseUpdaterFinder.find(Mockito.any(CcdCallbackDto.class))).thenReturn(Optional.of(ccdCaseUpdater));
        Mockito.when(ccdCaseUpdater.updateCase(Mockito.any(CcdCallbackDto.class))).thenReturn(jsonNode);
        ccdCallbackHandlerService.handleCddCallback(new CcdCallbackDto());
        Mockito.verify(ccdCaseUpdater, Mockito.times(1)).updateCase(Mockito.any(CcdCallbackDto.class));
    }
}