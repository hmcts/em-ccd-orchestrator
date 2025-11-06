package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.em.orchestrator.config.Constants;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingServiceException;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.CdamDto;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class CcdBundleStitchingServiceTest {

    @Mock
    private StitchingService stitchingService;

    private CcdBundleStitchingService ccdBundleStitchingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TOKEN = "jwt";

    private AutoCloseable openMocks;

    private ValidatorFactory validatorFactory;

    @BeforeEach
    void setup() {
        openMocks = MockitoAnnotations.openMocks(this);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        ccdBundleStitchingService = new CcdBundleStitchingService(objectMapper, stitchingService, validator);
        validatorFactory = factory;
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    void testUpdateCase() throws Exception {
        CcdDocument ccdDocument = new CcdDocument("", "", "");
        BDDMockito.given(stitchingService.stitch(any(), any(CdamDto.class))).willReturn(ccdDocument);
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":{\"eligibleForStitching\":\"yes\"}},"
            + "{\"value\":{}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt(TOKEN);
        ccdBundleStitchingService.updateCase(ccdCallbackDto);

        assertEquals(2, node.get("cb").size());
        assertEquals("", node.get("cb").get(0).path("value").path("stitchedDocument").path("document_url").textValue());
        assertNull(node.get("cb").get(1).path("value").path("stitchedDocument").path("document_url").textValue());

        Mockito.verify(stitchingService, Mockito.times(1))
                .stitch(Mockito.any(CcdBundleDTO.class), Mockito.any(CdamDto.class));
    }

    @Test
    void testUpdateCaseStitchingException() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":"
            + "{\"eligibleForStitching\":\"yes\"}},{\"value\":{}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt(TOKEN);

        Mockito.when(stitchingService.stitch(any(CcdBundleDTO.class), any(CdamDto.class)))
                .thenThrow(new StitchingServiceException("x"));

        assertThrows(StitchingServiceException.class, () -> ccdBundleStitchingService.updateCase(ccdCallbackDto));
    }

    @Test
    void testInvalidFilename() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":"
            + "{\"eligibleForStitching\":\"yes\", \"fileName\":\"$.pdf\"}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt(TOKEN);

        assertThrows(InputValidationException.class, () -> ccdBundleStitchingService.updateCase(ccdCallbackDto));
    }

    @Test
    void testUpdateCaseInterruptedException() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":"
            + "{\"eligibleForStitching\":\"yes\"}},{\"value\":{}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt(TOKEN);

        Mockito.when(stitchingService.stitch(any(CcdBundleDTO.class), any(CdamDto.class)))
                .thenThrow(new InterruptedException("x"));

        assertThrows(StitchingServiceException.class, () -> ccdBundleStitchingService.updateCase(ccdCallbackDto));
    }

    @Test
    void testUpdateCaseMissingBundles() {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setCaseData(objectMapper.getNodeFactory().arrayNode());
        JsonNode node = ccdBundleStitchingService.updateCase(ccdCallbackDto);
        assertNull(node.get(0));
    }

    @Test
    void testUpdateCaseFileNameOneChar() throws Exception {

        try {
            CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
            JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":{\"eligibleForStitching\":\"yes\", "
                + "\"fileName\":\"a\"}}]}");
            ccdCallbackDto.setPropertyName(Optional.of("cb"));
            ccdCallbackDto.setCaseData(node);
            ccdCallbackDto.setJwt(TOKEN);

            ccdBundleStitchingService.updateCase(ccdCallbackDto);
        } catch (InputValidationException exc) {
            assertEquals(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG, exc.getViolations().getFirst());
        }
    }

    @Test
    void testUpdateCaseFileName51Char() throws Exception {

        try {
            CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
            JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":{\"eligibleForStitching\":\"yes\", "
                + "\"fileName\":\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"}}]}");
            ccdCallbackDto.setPropertyName(Optional.of("cb"));
            ccdCallbackDto.setCaseData(node);
            ccdCallbackDto.setJwt(TOKEN);

            ccdBundleStitchingService.updateCase(ccdCallbackDto);
        } catch (InputValidationException exc) {
            assertEquals(Constants.STITCHED_FILE_NAME_FIELD_LENGTH_ERROR_MSG, exc.getViolations().getFirst());
        }
    }

}
