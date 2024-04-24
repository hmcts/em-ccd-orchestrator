package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.command;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.LocalConfigurationLoader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ValidateYamlCommandTest {

    @Mock
    private LocalConfigurationLoader loader;

    @InjectMocks
    private ValidateYamlCommand command;

    @Test
    public void run() {
        Mockito
            .when(loader.load(Mockito.any()))

            .thenReturn(null);

        boolean result = command.run();

        assertTrue(result);
    }

    @Test
    public void runWithError() {
        Mockito
            .when(loader.load(Mockito.matches("example.yaml")))
            .thenThrow(new RuntimeException(""));

        boolean result = command.run();

        assertFalse(result);
    }
}
