package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
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
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CdamDto;

import java.util.Optional;

import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class AsyncCcdBundleStitchingServiceTest {

    @Mock
    private AutomatedStitchingExecutor automatedStitchingExecutor;

    private AsyncCcdBundleStitchingService asyncCcdBundleStitchingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        asyncCcdBundleStitchingService =
            new AsyncCcdBundleStitchingService(objectMapper, automatedStitchingExecutor, validator);
    }

    @Test
    public void testUpdateCase() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":"
            + "{\"eligibleForStitching\":\"yes\"}},{\"value\":{}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");
        asyncCcdBundleStitchingService.updateCase(ccdCallbackDto);

        Mockito.verify(automatedStitchingExecutor, Mockito.times(1))
                .startStitching(Mockito.any(CdamDto.class), Mockito.any());
    }

    @Test(expected = InputValidationException.class)
    public void testInvalidFilename() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":"
            + "{\"eligibleForStitching\":\"yes\", \"fileName\":\"$.pdf\"}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");

        asyncCcdBundleStitchingService.updateCase(ccdCallbackDto);
    }

    @Test(expected = StitchingServiceException.class)
    public void testUpdateStitchingServiceException() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":"
            + "{\"eligibleForStitching\":\"yes\"}},{\"value\":{}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");

        Mockito.doThrow(new StitchingServiceException("x"))
                .when(automatedStitchingExecutor)
                .startStitching(Mockito.any(CdamDto.class), Mockito.any());

        asyncCcdBundleStitchingService.updateCase(ccdCallbackDto);
    }

    @Test
    public void testUpdateCaseMissingBundles() {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setCaseData(objectMapper.getNodeFactory().arrayNode());
        JsonNode node = asyncCcdBundleStitchingService.updateCase(ccdCallbackDto);
        assertNull(node.get(0));
    }

}
