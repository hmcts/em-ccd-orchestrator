package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.exampleservice.ExampleAddCaseBundleService;
import uk.gov.hmcts.reform.em.orchestrator.exampleservice.ExampleBundlePopulator;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.JsonNodesVerifier;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ExampleAddCaseBundleServiceTest {

    @Mock
    ExampleBundlePopulator exampleBundlePopulator;

    @Mock
    JsonNodesVerifier exampleCaseVerifier;

    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    ExampleAddCaseBundleService exampleAddCaseBundleService;

    @Test
    public void handles() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setJwt("x");

        ccdCallbackDto.setCcdPayload(objectMapper.readTree(
                "{\"caseBundles\": [{\"x\":\"y\"}], " +
                        "\"case_details\": {\"jurisdiction\":\"PUBLICLAW\", " +
                        "\"case_type_id\":\"CCD_BUNDLE_MVP_TYPE\"}}"));
        ccdCallbackDto.setPropertyName(Optional.of("caseBundles"));
        exampleAddCaseBundleService.handles(ccdCallbackDto);
        Mockito.verify(exampleCaseVerifier, Mockito.times(1)).verify(Mockito.any(JsonNode.class));
    }

    @Test
    public void updateCase() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setJwt("x");
        ccdCallbackDto.setCaseData(objectMapper.readTree("{\"caseBundles\": [{\"x\":\"y\"}]}"));
        ccdCallbackDto.setPropertyName(Optional.of("caseBundles"));
        exampleAddCaseBundleService.updateCase(ccdCallbackDto);
        Mockito.verify(exampleBundlePopulator, Mockito.times(1)).populateNewBundle(Mockito.any(JsonNode.class));
    }

}
