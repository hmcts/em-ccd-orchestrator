package uk.gov.hmcts.reform.em.orchestrator.automatedbundling.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.configuration.LocalConfigurationLoader;

import java.io.File;
import java.net.URL;

@Service
public class ValidateYamlCommand {

    private final LocalConfigurationLoader loader;

    private static final Logger log = LoggerFactory.getLogger(ValidateYamlCommand.class);

    @Autowired
    public ValidateYamlCommand(LocalConfigurationLoader loader) {
        this.loader = loader;
    }

    public boolean run() {
        URL url = Thread
            .currentThread()
            .getContextClassLoader()
            .getResource("bundleconfiguration/");

        File folder = new File(url.getPath());
        int numErrors = 0;

        for (final File file : folder.listFiles()) {
            if (file.isFile()) {
                try {
                    loader.load(file.getName());
                    log.info("Validating {} succeeded", file.getName());
                } catch (Exception e) {
                    log.error("Validating {} failed", file.getName(), e.getCause());
                    numErrors++;
                }
            }
        }

        return numErrors == 0;
    }

}
