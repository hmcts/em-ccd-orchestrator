package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.command;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.LocalConfigurationLoader;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ValidateYamlCommandTest {

    @Mock
    private LocalConfigurationLoader loader;

    @InjectMocks
    private ValidateYamlCommand command;

    @Test
    void run() {
        Mockito
            .when(loader.load(Mockito.any()))

            .thenReturn(null);

        boolean result = command.run();

        assertTrue(result);
    }

    @Test
    void runWithError() {
        Mockito
            .when(loader.load(Mockito.matches("example.yaml")))
            .thenThrow(new RuntimeException(""));

        boolean result = command.run();

        assertFalse(result);
    }
}
