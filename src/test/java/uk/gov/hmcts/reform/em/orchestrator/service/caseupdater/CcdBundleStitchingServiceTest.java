package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import pl.touk.throwing.exception.WrappedException;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingServiceException;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class CcdBundleStitchingServiceTest {

    @Mock
    private StitchingService stitchingService;

    private CcdBundleStitchingService ccdBundleStitchingService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        CcdDocument ccdDocument = new CcdDocument("", "", "");
        BDDMockito.given(stitchingService.stitch(any(), any())).willReturn(ccdDocument);
        ccdBundleStitchingService = new CcdBundleStitchingService(objectMapper, stitchingService);
    }

    @Test
    public void testUpdateCase() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":{\"eligibleForStitching\":\"yes\"}},{\"value\":{}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");
        ccdBundleStitchingService.updateCase(ccdCallbackDto);

        Assert.assertEquals(2, ((ArrayNode)node.get("cb")).size());
        Assert.assertEquals("", node.get("cb").get(0).path("value").path("stitchedDocument").path("document_url").textValue());
        Assert.assertNull(node.get("cb").get(1).path("value").path("stitchedDocument").path("document_url").textValue());

        Mockito.verify(stitchingService, Mockito.times(1))
                .stitch(Mockito.any(CcdBundleDTO.class), Mockito.any(String.class));
    }

    @Test(expected = WrappedException.class)
    public void testUpdateCaseStitchingException() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\":[{\"value\":{\"eligibleForStitching\":\"yes\"}},{\"value\":{}}]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");

        Mockito.when(stitchingService.stitch(Mockito.any(CcdBundleDTO.class), Mockito.any(String.class))).thenThrow(new StitchingServiceException("x"));

        ccdBundleStitchingService.updateCase(ccdCallbackDto);


    }

}