package uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import java.util.function.Supplier;

import static pl.touk.throwing.ThrowingSupplier.unchecked;

@Service
public class CcdCallbackDtoCreator {

    private final ObjectMapper objectMapper;

    @Value("${cdam.validation.enabled}")
    private boolean enableCdamValidation;

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
        return createCcdCallbackDto(unchecked(() -> objectMapper.readTree(reader)), propertyName, jwt);
    }

    public CcdCallbackDto createDto(String propertyName, String jwt, StartEventResponse startEventResponse) {
        return createCcdCallbackDto(unchecked(() -> objectMapper.valueToTree(startEventResponse)), propertyName, jwt);
    }

    private CcdCallbackDto createCcdCallbackDto(Supplier<JsonNode> readTree, String propertyName, String jwt) {
        CcdCallbackDto dto = new CcdCallbackDto();
        JsonNode payload;
        try {
            payload = readTree.get();
        } catch (Exception ex) {
            throw new CantReadCcdPayloadException("Payload from CCD can't be read", ex);
        }
        if (payload == null) {
            throw new CantReadCcdPayloadException("Payload from CCD is empty");
        }
        dto.setCcdPayload(payload);
        dto.setCaseData(dto.getCcdPayload().findValue("case_data"));
        dto.setCaseDetails(dto.getCcdPayload().findValue("case_details"));
        dto.setPropertyName(Optional.of(propertyName));
        dto.setJwt(jwt);
        dto.setEnableCdamValidation(enableCdamValidation);
        return dto;
    }

}
