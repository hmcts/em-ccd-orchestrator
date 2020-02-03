package uk.gov.hmcts.reform.em.orchestrator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import uk.gov.hmcts.reform.em.orchestrator.automatedbundling.command.ValidateYamlCommand;
import uk.gov.hmcts.reform.em.orchestrator.config.SwaggerConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
@ComponentScan(excludeFilters  = {@ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE, classes = {Application.class, SwaggerConfiguration.class})})
public class CliApplication implements ApplicationRunner {

    @Autowired
    private ValidateYamlCommand validateYamlCommand;

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(CliApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.run(args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        /*if (args.containsOption("validate")) {
            boolean isValid = validateYamlCommand.run();

            if (!isValid) {
                System.exit(-1);
            }
        }*/
    }
}
