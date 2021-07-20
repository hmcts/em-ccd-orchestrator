package uk.gov.hmcts.reform.em.orchestrator.service;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.em.orchestrator.config.security.SecurityUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Aspect
@Component
public class ServiceNameAspect {

    private final Logger log = LoggerFactory.getLogger(ServiceNameAspect.class);

    private static final String BEARER = "Bearer ";

    @Autowired
    SecurityUtils securityUtils;

    @Before(
        "execution(* uk.gov.hmcts.reform.em.orchestrator.endpoint.CcdCloneBundleController.*(..)) ||"
            + " execution(* uk.gov.hmcts.reform.em.orchestrator.endpoint.CcdStitchBundleCallbackController.*(..)) ||"
            + " execution(* uk.gov.hmcts.reform.em.orchestrator.endpoint.NewBundleController.*(..)) ||"
            +  " execution(* uk.gov.hmcts.reform.em.orchestrator.endpoint.StitchingCompleteCallbackController.*(..))")
    public void logServiceName() {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        if (Objects.nonNull(request)) {
            String s2sToken = request.getHeader("serviceauthorization");
            if (StringUtils.isNotBlank(s2sToken)) {
                String serviceName;
                if (s2sToken.startsWith(BEARER)) {
                    serviceName = securityUtils.getServiceName(s2sToken);
                } else {
                    serviceName = securityUtils.getServiceName(BEARER + s2sToken);
                }
                log.info("Endpoint : {}  for : {} method is accessed by {} ", request.getRequestURI(),
                    request.getMethod(), serviceName);
            }
        }
    }
}
