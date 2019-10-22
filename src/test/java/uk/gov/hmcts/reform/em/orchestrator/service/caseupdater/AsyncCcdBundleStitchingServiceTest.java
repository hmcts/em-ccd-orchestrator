package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedStitchingExecutor;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingServiceException;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class AsyncCcdBundleStitchingServiceTest {

    @Mock
    private AutomatedStitchingExecutor automatedStitchingExecutor;

    private AsyncCcdBundleStitchingService asyncCcdBundleStitchingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        asyncCcdBundleStitchingService = new AsyncCcdBundleStitchingService(objectMapper, automatedStitchingExecutor, validator);
    }

    @Test
    public void testUpdateCase() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":{\"eligibleForStitching\":\"yes\"}},{\"value\":{}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");
        asyncCcdBundleStitchingService.updateCase(ccdCallbackDto);

        Mockito.verify(automatedStitchingExecutor, Mockito.times(1))
                .startStitching(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test(expected = InputValidationException.class)
    public void testInvalidFilename() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":{\"eligibleForStitching\":\"yes\", \"fileName\":\"$.pdf\"}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");

        asyncCcdBundleStitchingService.updateCase(ccdCallbackDto);
    }

    @Test
    public void testHandles() {
        assertFalse(asyncCcdBundleStitchingService.handles(null));
    }

    @Test(expected = StitchingServiceException.class)
    public void testUpdateStitchingServiceException() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":{\"eligibleForStitching\":\"yes\"}},{\"value\":{}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");

        Mockito.doThrow(new StitchingServiceException("x"))
                .when(automatedStitchingExecutor)
                .startStitching(Mockito.any(), Mockito.any(), Mockito.any());

        asyncCcdBundleStitchingService.updateCase(ccdCallbackDto);
    }

    @Test
    public void testUpdateCaseMissingBundles() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setCaseData(objectMapper.getNodeFactory().arrayNode());
        JsonNode node = asyncCcdBundleStitchingService.updateCase(ccdCallbackDto);
        assertNull(node.get(0));
    }

}
