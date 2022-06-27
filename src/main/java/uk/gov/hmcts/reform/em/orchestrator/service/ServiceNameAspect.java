package uk.gov.hmcts.reform.em.orchestrator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.em.orchestrator.config.Constants;
import uk.gov.hmcts.reform.em.orchestrator.config.security.SecurityUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.Reader;

@Aspect
@Component
public class ServiceNameAspect {

    private final Logger log = LoggerFactory.getLogger(ServiceNameAspect.class);

    private static final String BEARER = "Bearer ";

    @Autowired
    SecurityUtils securityUtils;

    @Autowired
    ObjectMapper objectMapper;

    @Before(
        "execution(* uk.gov.hmcts.reform.em.orchestrator.endpoint.CcdCloneBundleController.*(..)) ||"
            + " execution(* uk.gov.hmcts.reform.em.orchestrator.endpoint.CcdStitchBundleCallbackController.*(..)) ||"
            + " execution(* uk.gov.hmcts.reform.em.orchestrator.endpoint.NewBundleController.*(..)) ||"
            +  " execution(* uk.gov.hmcts.reform.em.orchestrator.endpoint.StitchingCompleteCallbackController.*(..))")
    public void logServiceName() {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String s2sToken = request.getHeader("serviceauthorization");
        if (StringUtils.isNotBlank(s2sToken)) {
            try {
                String serviceName;
                if (s2sToken.startsWith(BEARER)) {
                    serviceName = securityUtils.getServiceName(s2sToken);
                } else {
                    serviceName = securityUtils.getServiceName(BEARER + s2sToken);
                }
                String caseId = getCaseId(request.getReader(), request.getRequestURI());
                log.info("em-ccdorc : Endpoint : {}  for : {} method is accessed by {} with caseId as {}",
                        request.getRequestURI(), request.getMethod(), serviceName, caseId);
            } catch (InvalidTokenException | IOException invalidTokenException) {
                log.warn("invalidTokenException logged is: {} ", invalidTokenException.getMessage());
            }
        }
    }

    private String getCaseId(Reader reader, String uri) throws IOException {
        JsonNode payload = objectMapper.readTree(reader);
        log.debug("Bundling request to {} has payload : {} ",uri, payload);
        return payload.findValue(Constants.ID) != null ? payload.findValue(Constants.ID).asText() : StringUtils.EMPTY;
    }
}
