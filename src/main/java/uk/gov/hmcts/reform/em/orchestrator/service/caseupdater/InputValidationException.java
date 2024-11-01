package uk.gov.hmcts.reform.em.orchestrator.service.caseupdater;

import jakarta.validation.ConstraintViolation;
import uk.gov.hmcts.reform.em.orchestrator.service.dto.CcdBundleDTO;

import java.util.List;
import java.util.Set;

public class InputValidationException extends RuntimeException {

    private final List<String> violations;

    public InputValidationException(Set<ConstraintViolation<CcdBundleDTO>> violations) {
        super("Bundle input validation error, violations: "
                + String.join(", ", violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList()));

        this.violations = violations.stream()
            .map(ConstraintViolation::getMessage)
            .toList();
    }

    public List<String> getViolations() {
        return this.violations;
    }
}
