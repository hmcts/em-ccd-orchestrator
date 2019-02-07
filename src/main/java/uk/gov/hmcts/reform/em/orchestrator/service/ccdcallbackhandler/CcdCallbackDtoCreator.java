package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

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

    public CcdCallbackDto createDto(HttpServletRequest request) throws IOException {
        CcdCallbackDto dto = new CcdCallbackDto();
        dto.setJwt(request.getHeader("Authorization"));
        dto.setCcdPaylod(objectMapper.readTree(request.getReader()));
        dto.setCaseData(dto.getCcdPaylod().findValue("case_data"));
        return dto;
    }

    public CcdCallbackDto createDto(HttpServletRequest request, String propertyName) throws IOException {
        CcdCallbackDto dto = createDto(request);
        dto.setPropertyName(Optional.ofNullable(propertyName));
        return dto;
    }

}
