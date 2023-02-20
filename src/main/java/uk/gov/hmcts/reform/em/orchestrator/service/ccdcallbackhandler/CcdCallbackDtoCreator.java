package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

@Service
public class CcdCallbackDtoCreator {

    private final ObjectMapper objectMapper;

    public CcdCallbackDtoCreator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CcdCallbackDto createDto(HttpServletRequest request) {
        return createDto(request, "caseBundles");
    }

    public CcdCallbackDto createDto(HttpServletRequest request, String propertyName) {
        try {
            return this.createDto(propertyName, request.getHeader("Authorization"), request.getReader());
        } catch (IOException e) {
            throw new CantReadCcdPayloadException("Payload from CCD can't be read", e);
        }
    }

    public CcdCallbackDto createDto(String propertyName, String jwt, Reader reader) {
        CcdCallbackDto dto = new CcdCallbackDto();
        try {
            JsonNode payload = objectMapper.readTree(reader);
            if (payload == null) {
                throw new CantReadCcdPayloadException("Payload from CCD is empty");
            }
            dto.setCcdPayload(payload);
        } catch (IOException e) {
            throw new CantReadCcdPayloadException("Payload from CCD can't be read", e);
        }
        dto.setCaseData(dto.getCcdPayload().findValue("case_data"));
        dto.setCaseDetails(dto.getCcdPayload().findValue("case_details"));
        dto.setPropertyName(Optional.of(propertyName));
        dto.setJwt(jwt);
        return dto;
    }

    public CcdCallbackDto createDto(String propertyName, String jwt, StartEventResponse startEventResponse) {
        CcdCallbackDto dto = new CcdCallbackDto();
        try {
            dto.setCcdPayload(objectMapper.valueToTree(startEventResponse));
        } catch (Exception e) {
            throw new CantReadCcdPayloadException("Payload from CCD can't be read", e);
        }
        dto.setCaseData(dto.getCcdPayload().findValue("case_data"));
        dto.setCaseDetails(dto.getCcdPayload().findValue("case_details"));
        dto.setPropertyName(Optional.of(propertyName));
        dto.setJwt(jwt);
        return dto;
    }

}
