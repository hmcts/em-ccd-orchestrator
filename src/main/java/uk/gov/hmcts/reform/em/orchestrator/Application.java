package uk.gov.hmcts.reform.em.orchestrator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.command.ValidateYamlCommand;

import java.util.Arrays;

@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.reform.em.orchestrator",
    "uk.gov.hmcts.reform.authorisation",
    "uk.gov.hmcts.reform.ccd.client"},
    exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class}
)
@EnableScheduling
public class Application implements CommandLineRunner {

    private final ValidateYamlCommand validateYamlCommand;

    public Application(ValidateYamlCommand validateYamlCommand) {
        this.validateYamlCommand = validateYamlCommand;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        if (Arrays.asList(args).contains("--validate")) {
            if (!validateYamlCommand.run()) {
                System.exit(-1);
            }
            System.exit(0);
        }
    }

}
