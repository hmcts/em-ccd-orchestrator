package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.CcdCaseUpdater;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CcdCaseUpdaterFinderImplTest {

    @Test
    public void find() {

        CcdCaseUpdater ccdCaseUpdater = Mockito.mock(CcdCaseUpdater.class);

        CcdCaseUpdaterFinderImpl ccdCaseUpdaterFinder =
                new CcdCaseUpdaterFinderImpl(Stream.of(ccdCaseUpdater).collect(Collectors.toList()));

        Mockito.when(ccdCaseUpdater.handles(Mockito.any(CcdCallbackDto.class))).thenReturn(true);

        Optional<CcdCaseUpdater> foundCcdCaseUpdater = ccdCaseUpdaterFinder.find(new CcdCallbackDto());

        Assert.assertEquals(ccdCaseUpdater, foundCcdCaseUpdater.get());

    }
}
