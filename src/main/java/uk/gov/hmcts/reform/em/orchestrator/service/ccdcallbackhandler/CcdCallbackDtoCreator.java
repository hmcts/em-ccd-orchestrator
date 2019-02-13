package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

@Service
public class CcdCallbackDtoCreator {

    private final ObjectMapper objectMapper;

    public CcdCallbackDtoCreator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public CcdCallbackDto createDto(HttpServletRequest request) {
        CcdCallbackDto dto = new CcdCallbackDto();
        dto.setJwt(request.getHeader("Authorization"));
        try {
            JsonNode payload = objectMapper.readTree(request.getReader());
            if (payload == null) {
                throw new CantReadCcdPayloadException("Payload from CCD is empty");
            }
            dto.setCcdPayload(payload);
        } catch (IOException e) {
            throw new CantReadCcdPayloadException("Payload from CCD can't be read", e);
        }
        dto.setCaseData(dto.getCcdPayload().findValue("case_data"));
        return dto;
    }

    public CcdCallbackDto createDto(HttpServletRequest request, String propertyName) {
        CcdCallbackDto dto = createDto(request);
        dto.setPropertyName(Optional.ofNullable(propertyName));
        return dto;
    }

}
