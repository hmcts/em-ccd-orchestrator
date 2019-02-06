package uk.gov.hmcts.reform.em.orchestrator.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService;
import uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingServiceException;

import java.io.IOException;

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
        BDDMockito.given(stitchingService.stitch(any(), any())).willReturn("AAAAA");
        ccdBundleStitchingService = new CcdBundleStitchingService(stitchingService);
    }

    @Test
    public void testUpdateCase() throws IOException {
        JsonNode node = objectMapper.readTree("[{ }]");
        ccdBundleStitchingService.updateCase(node, "jwt");

        String stitchedDocId = node.get(0).get("stitchedDocId").textValue();
        Assert.assertEquals("AAAAA", stitchedDocId);
    }

    @Test(expected = IncorrectCcdCaseBundlesException.class)
    public void testUpdateCaseNodeNotArray() throws Exception {
        JsonNode node = objectMapper.readTree("{}");
        ccdBundleStitchingService.updateCase(node, "jwt");
    }

}