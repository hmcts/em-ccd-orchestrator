package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import jakarta.validation.ConstraintViolation;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class InputValidationException extends RuntimeException {

    private final List<String> violations;

    public InputValidationException(Set<ConstraintViolation<CcdBundleDTO>> violations) {
        super("Bundle input validation error");

        this.violations = violations.stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.toList());
    }

    public List<String> getViolations() {
        return this.violations;
    }
}
