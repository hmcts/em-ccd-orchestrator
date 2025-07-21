package uk.gov.hmcts.reform.em.orchestrator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.command.ValidateYamlCommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationRunnerTest {

    @Mock
    private ValidateYamlCommand mockValidateYamlCommand;

    @InjectMocks
    private ValidationRunner validationRunner;

    @Test
    void runValidationSucceeds() {
        when(mockValidateYamlCommand.run()).thenReturn(true);
        validationRunner.run();

        assertEquals(0, validationRunner.getExitCode());
        verify(mockValidateYamlCommand).run();
    }

    @Test
    void runValidationFails() {
        when(mockValidateYamlCommand.run()).thenReturn(false);
        validationRunner.run();

        assertEquals(1, validationRunner.getExitCode());
        verify(mockValidateYamlCommand).run();
    }
}