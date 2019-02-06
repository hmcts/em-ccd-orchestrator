package uk.gov.hmcts.reform.em.orchestrator.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CcdBundleStitchingServiceTest {

    @InjectMocks
    CcdBundleStitchingService ccdBundleStitchingService;

    private ObjectMapper objectMapper = new ObjectMapper();

//  TODO: Happy path for callback to stitching API
//    @Test
//    public void testUpdateCase() throws Exception {
//    }

//    @Test(expected = IncorrectCcdCaseBundlesException.class)
//    public void testUpdateCaseNodeNotArray() throws Exception {
//        JsonNode node = objectMapper.readTree("{}");
//        ccdBundleStitchingService.updateCase(node, "jwt");
//    }

}