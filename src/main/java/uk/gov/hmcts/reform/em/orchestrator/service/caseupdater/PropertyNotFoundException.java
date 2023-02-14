package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import uk.gov.hmcts.reform.em.orchestrator.service.ccdcallbackhandler.CcdCallbackDto;

import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PropertyNotFoundException extends RuntimeException {

    private final List<String> violations;

    public PropertyNotFoundException(Set<ConstraintViolation<CcdCallbackDto>> violations) {
        super("Bundle request payload validation error");

        this.violations = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
    }

}
