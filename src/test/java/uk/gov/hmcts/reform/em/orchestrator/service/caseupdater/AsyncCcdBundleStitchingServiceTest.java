package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.AutomatedStitchingExecutor;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingServiceException;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CdamDto;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AsyncCcdBundleStitchingServiceTest {

    @Mock
    private AutomatedStitchingExecutor automatedStitchingExecutor;

    private AsyncCcdBundleStitchingService asyncCcdBundleStitchingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        asyncCcdBundleStitchingService =
            new AsyncCcdBundleStitchingService(objectMapper, automatedStitchingExecutor, validator);
    }

    @Test
    void testUpdateCase() throws Exception {
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

    @Test
    void testInvalidFilename() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":"
            + "{\"eligibleForStitching\":\"yes\", \"fileName\":\"$.pdf\"}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");
        assertThrows(InputValidationException.class, () -> asyncCcdBundleStitchingService.updateCase(ccdCallbackDto));
    }

    @Test
    void testUpdateStitchingServiceException() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":"
            + "{\"eligibleForStitching\":\"yes\"}},{\"value\":{}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");

        Mockito.doThrow(new StitchingServiceException("x"))
                .when(automatedStitchingExecutor)
                .startStitching(Mockito.any(CdamDto.class), Mockito.any());

        assertThrows(StitchingServiceException.class, () -> asyncCcdBundleStitchingService.updateCase(ccdCallbackDto));
    }

    @Test
    void testUpdateCaseMissingBundles() {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setCaseData(objectMapper.getNodeFactory().arrayNode());
        JsonNode node = asyncCcdBundleStitchingService.updateCase(ccdCallbackDto);
        assertNull(node.get(0));
    }

}
