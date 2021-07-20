package uk.gov.hmcts.reform.em.orchestrator.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

@Service
public class SecurityUtils {

    @Autowired
    private AuthTokenValidator authTokenValidator;

    public String getServiceName(final String token) {
        return authTokenValidator.getServiceName(token);
    }

}
