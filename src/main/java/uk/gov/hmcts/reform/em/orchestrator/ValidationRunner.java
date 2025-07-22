package uk.gov.hmcts.reform.em.orchestrator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.command.ValidateYamlCommand;

@Component
@Profile("validate")
public class ValidationRunner implements CommandLineRunner, ExitCodeGenerator {

    private final ValidateYamlCommand validateYamlCommand;
    private int exitCode = 0;

    public ValidationRunner(ValidateYamlCommand validateYamlCommand) {
        this.validateYamlCommand = validateYamlCommand;
    }

    @Override
    public void run(String... args) {
        if (!validateYamlCommand.run()) {
            this.exitCode = 1;
        }
    }

    @Override
    public int getExitCode() {
        return this.exitCode;
    }
}