package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.financialremedy;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.financialremedyservice.FinancialRemedyAddCaseBundleService;
import uk.gov.hmcts.reform.em.orchestrator.financialremedyservice.FinancialRemedyBundlePopulator;
import uk.gov.hmcts.reform.em.orchestrator.service.caseupdater.JsonNodesVerifier;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class FinancialRemedyAddCaseBundleServiceTest {

    @Mock
    FinancialRemedyBundlePopulator financialRemedyBundlePopulator;

    @Mock
    JsonNodesVerifier exampleCaseVerifier;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    FinancialRemedyAddCaseBundleService financialRemedyAddCaseBundleService;

    @Test
    public void handles() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setJwt("jwt");

        JSONObject data = new JSONObject();
        data.put("case_details", prepareCaseDetails());
        data.put("caseBundles", prepareBundle());


        ccdCallbackDto.setCcdPayload(objectMapper.readTree(data.toString()));
        ccdCallbackDto.setPropertyName(Optional.of("caseBundles"));
        financialRemedyAddCaseBundleService.handles(ccdCallbackDto);
        Mockito.verify(exampleCaseVerifier, Mockito.times(1)).verify(Mockito.any(JsonNode.class));
    }

    @Test
    public void updateCase() throws Exception {
        CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();
        ccdCallbackDto.setJwt("jwt");
        JSONObject data = new JSONObject();
        data.put("caseBundles", prepareBundle());
        ccdCallbackDto.setCaseData(objectMapper.readTree(data.toString()));
        ccdCallbackDto.setPropertyName(Optional.of("caseBundles"));
        financialRemedyAddCaseBundleService.updateCase(ccdCallbackDto);
        Mockito.verify(financialRemedyBundlePopulator, Mockito.times(1)).populateNewBundle(Mockito.any(JsonNode.class));
    }

    private static JSONObject prepareCaseDetails() {
        JSONObject caseDetails = new JSONObject();
        caseDetails.put("jurisdiction", "DIVORCE");
        caseDetails.put("case_type_id", "FinancialRemedyContested");
        return caseDetails;
    }


    private static JSONArray prepareBundle() {
        JSONArray caseBundleArray = new JSONArray();
        JSONObject caseBundle = new JSONObject();
        caseBundleArray.put(caseBundle.put("key", "value"));
        return caseBundleArray;
    }

}
