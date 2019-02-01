package uk.gov.hmcts.reform.em.stitching.service.callback.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.stitching.batch.DocumentTaskItemProcessor;
import uk.gov.hmcts.reform.em.stitching.domain.Bundle;
import uk.gov.hmcts.reform.em.stitching.domain.DocumentTask;
import uk.gov.hmcts.reform.em.stitching.service.dto.BundleDTO;
import uk.gov.hmcts.reform.em.stitching.service.mapper.BundleMapper;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CcdBundleStitchingServiceTest {

    @Mock
    DocumentTaskItemProcessor documentTaskItemProcessor;

    @Mock
    BundleMapper bundleMapper;

    @InjectMocks
    CcdBundleStitchingService ccdBundleStitchingService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testUpdateCase() throws Exception {

        Bundle bundle = new Bundle();

        DocumentTask documentTask = new DocumentTask();
        documentTask.setBundle(bundle);

        BundleDTO bundleDto = new BundleDTO();

        Mockito
            .when(bundleMapper.toEntity(Mockito.any(BundleDTO.class)))
            .thenReturn(bundle);

        Mockito
            .when(bundleMapper.toDto(Mockito.any(Bundle.class)))
            .thenReturn(bundleDto);

        Mockito
            .when(documentTaskItemProcessor.process(Mockito.any(DocumentTask.class)))
            .thenReturn(documentTask);

        JsonNode node = objectMapper.readTree("[{}]");

        ccdBundleStitchingService.updateCase(node, "jwt");

        assertEquals(1, node.size());

    }

    @Test(expected = IncorrectCcdCaseBundlesException.class)
    public void testUpdateCaseNodeNotArray() throws Exception {
        JsonNode node = objectMapper.readTree("{}");
        ccdBundleStitchingService.updateCase(node, "jwt");
    }

}