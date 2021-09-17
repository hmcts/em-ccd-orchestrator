package uk.gov.hmcts.reform.em.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.em.orchestrator.stitching.dto.DocumentTaskDTO;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Aspect
@Component
public class CdamAspect {

    private final Logger log = LoggerFactory.getLogger(CdamAspect.class);

    private static final String CASE_TYPE_ID = "case_type_id";
    private static final String JURISDICTION_ID = "jurisdiction";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before("execution(* uk.gov.hmcts.reform.em.orchestrator.stitching.StitchingService.startStitchingTask(..))")
    public void populateCdamDetails(JoinPoint joinPoint) {

        log.info("CdamAspect invoked");

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        try {
            JsonNode payload = objectMapper.readTree(request.getReader());
            if (Objects.nonNull(payload) && Objects.nonNull(payload.findValue(CASE_TYPE_ID))
                && Objects.nonNull(payload.findValue(JURISDICTION_ID))) {

                Optional<DocumentTaskDTO> optDocumentTaskDto = Arrays.stream(joinPoint.getArgs())
                    .filter(DocumentTaskDTO.class::isInstance)
                    .map(DocumentTaskDTO.class::cast)
                    .findFirst();

                if (optDocumentTaskDto.isPresent()) {
                    DocumentTaskDTO documentTaskDTO = optDocumentTaskDto.get();
                    documentTaskDTO.setServiceAuth(request.getHeader("serviceauthorization"));
                    documentTaskDTO.setCaseTypeId(payload.findValue(CASE_TYPE_ID).asText());
                    documentTaskDTO.setJurisdictionId(payload.findValue(JURISDICTION_ID).asText());
                    log.info("Cdam details populated");
                }
            }
        } catch (IOException e) {
            log.warn(String.format("Could not get the CaseTypeId , Jurisdiction : %s", e.getMessage()));
        }

        log.info("CdamAspect completed");
    }
}
