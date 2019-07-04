package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;

import javax.validation.ConstraintViolation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InputValidationException extends RuntimeException {
    public final List<String> violations;

    public InputValidationException(Set<ConstraintViolation<CcdBundleDTO>> violations) {
        super("Bundle input validation error");

        this.violations = violations.stream()
            .map(v -> v.getMessage())
            .collect(Collectors.toList());
    }
}
