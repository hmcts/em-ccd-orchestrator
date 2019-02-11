package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdDocument;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingServiceException;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class CcdBundleStitchingServiceTest {

    @Mock
    private StitchingService stitchingService;

    private CcdBundleStitchingService ccdBundleStitchingService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() throws StitchingServiceException, InterruptedException {
        MockitoAnnotations.initMocks(this);
        CcdDocument ccdDocument = new CcdDocument("", "", "");
        BDDMockito.given(stitchingService.stitch(any(), any())).willReturn(ccdDocument);
        ccdBundleStitchingService = new CcdBundleStitchingService(objectMapper, stitchingService);
    }

    @Test
    public void testUpdateCase() throws IOException {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        JsonNode node = objectMapper.readTree("{\"cb\": [{ \"value\": {} }]}");
        ccdCallbackDto.setPropertyName(Optional.of("cb"));
        ccdCallbackDto.setCaseData(node);
        ccdCallbackDto.setJwt("jwt");
        ccdBundleStitchingService.updateCase(ccdCallbackDto);

        String stitchedDocumentURI = node.get("cb").get(0).path("value").path("stitchedDocument").path("document_url").textValue();
        Assert.assertEquals("", stitchedDocumentURI);
    }


}