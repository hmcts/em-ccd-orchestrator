package uk.gov.hmcts.reform.em.orchestrator.service.ccdapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseResource;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CantReadCcdPayloadException;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;
import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDtoCreator;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CCDUpdateServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdCallbackDtoCreator ccdCallbackDtoCreator;

    @InjectMocks
    private CcdUpdateService ccdUpdateService;

    private ObjectMapper objectMapper = new ObjectMapper();
    private CcdCallbackDto ccdCallbackDto = new CcdCallbackDto();

    private String caseId = "test_case_id";
    private String triggerId = "event_abc";
    private String jwt = "jtw_test";
    private String serviceToken = "service_token";
    private String ccdEventToken = "ccd_event_token";

    private Map<String, Object> caseDataMap = Map.of("id", "23132131", "caseCustomData", Map.of("key1", "value"));

    private CaseDetails caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .caseTypeId(caseId)
            .callbackResponseStatus("SUCCESS")
            .jurisdiction("TEST_jurisdiction")
            .build();

    private StartEventResponse startEventResponse = StartEventResponse
            .builder()
            .token(ccdEventToken)
            .eventId(triggerId)
            .caseDetails(caseDetails)
            .build();

    @Test
    void should_startCcdEvent() {
        given(authTokenGenerator.generate()).willReturn(serviceToken);
        ccdUpdateService.startCcdEvent(caseId, triggerId, jwt);
        verify(coreCaseDataApi).startEvent(jwt, serviceToken, caseId, triggerId);
    }

    @Test
    void should_return_startCcdEvent_response() {
        given(authTokenGenerator.generate()).willReturn(serviceToken);
        given(coreCaseDataApi.startEvent(jwt, serviceToken, caseId, triggerId)).willReturn(startEventResponse);
        given(ccdCallbackDtoCreator.createDto("caseBundles", jwt, startEventResponse)).willReturn(ccdCallbackDto);

        try {
            ccdCallbackDto.setCcdPayload(objectMapper.valueToTree(startEventResponse));
        } catch (Exception e) {
            throw new CantReadCcdPayloadException("Payload from CCD can't be read", e);
        }
        ccdCallbackDto.setCaseData(ccdCallbackDto.getCcdPayload().findValue("case_data"));
        ccdCallbackDto.setCaseDetails(ccdCallbackDto.getCcdPayload().findValue("case_details"));
        ccdCallbackDto.setJwt(jwt);
        ccdCallbackDto.setPropertyName(Optional.of("caseBundles"));


        CcdCallbackDto result = ccdUpdateService.startCcdEvent(caseId, triggerId, jwt);

        verify(coreCaseDataApi).startEvent(jwt, serviceToken, caseId, triggerId);
        assertThat(result.getJwt()).isEqualTo(jwt);
        assertThat(result.getPropertyName().get()).contains("caseBundles");

        assertThatObject(result.getCaseData())
                .extracting(jn -> textAtPath(jn, "/id"), jn -> textAtPath(jn, "/caseCustomData/key1"))
                .containsExactly("23132131", "value");
    }

    private String textAtPath(JsonNode node, String path) {
        System.out.println("node[" + node + "]");
        return node.at(path).asText();
    }

    @Test
    void should_call_createCcdEvent() {
        given(authTokenGenerator.generate()).willReturn(serviceToken);
        ccdCallbackDto.setCcdPayload(objectMapper.valueToTree(startEventResponse));
        ccdCallbackDto.setCaseData(objectMapper.valueToTree(caseDataMap));
        ccdCallbackDto.setCaseDetails(objectMapper.valueToTree(caseDetails));


        CaseResource caseResource = new CaseResource();
        caseResource.setData(Map.of("caseBundles", objectMapper.valueToTree(Map.of("key123", "value123"))));
        given(coreCaseDataApi.createEvent(anyString(), anyString(), anyString(), any(CaseDataContent.class)))
                .willReturn(caseResource);

        ccdUpdateService.submitCcdEvent(caseId, jwt, ccdCallbackDto);
        ArgumentCaptor<CaseDataContent> caseDataContentCapturer = ArgumentCaptor.forClass(CaseDataContent.class);
        verify(coreCaseDataApi).createEvent(eq(jwt), eq(serviceToken), eq(caseId), caseDataContentCapturer.capture());
        var caseDataContent = caseDataContentCapturer.getValue();
        assertThat(caseDataContent.getEvent()).isEqualTo(Event.builder().id(triggerId).build());
        assertThat(caseDataContent.getEventToken()).isEqualTo(ccdEventToken);

        assertThatObject(caseDataContent.getData())
                .extracting(jn -> textAtPath((JsonNode) jn, "/id"),
                    jn -> textAtPath((JsonNode) jn, "/caseCustomData/key1"))
                .containsExactly("23132131", "value");

    }
}
