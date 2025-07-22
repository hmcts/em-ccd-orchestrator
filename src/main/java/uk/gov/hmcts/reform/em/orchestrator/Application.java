package uk.gov.hmcts.reform.em.orchestrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.reform.em.orchestrator",
    "uk.gov.hmcts.reform.authorisation",
    "uk.gov.hmcts.reform.ccd.client"}
)
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, its not a utility class
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}