package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.command;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.em.orchestrator.CliApplication;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.LocalConfigurationLoader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CliApplication.class})
public class ValidateYamlCommandTest {

    @MockBean
    private LocalConfigurationLoader loader;

    @Test
    public void run() {
        Mockito
            .when(loader.load(Mockito.any()))
            .thenReturn(null);

        ValidateYamlCommand command = new ValidateYamlCommand(loader);
        boolean result = command.run();

        assertTrue(result);

    }

    @Test
    public void runWithError() {
        Mockito
            .when(loader.load(Mockito.matches("example.yaml")))
            .thenThrow(new RuntimeException(""));

        ValidateYamlCommand command = new ValidateYamlCommand(loader);
        boolean result = command.run();

        assertFalse(result);
    }
}
