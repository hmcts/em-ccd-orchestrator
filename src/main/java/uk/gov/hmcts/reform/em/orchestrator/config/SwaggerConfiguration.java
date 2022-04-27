package uk.gov.hmcts.reform.em.orchestrator.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI api() {
        return new OpenAPI()
                .info(
                        new Info().title("EM CCD Orchestrator")
                                .description("Orchestrates callbacks from CCD relating to management and "
                                        + "stitching of bundles. caseTypeId & jurisdictionId "
                                        + "are required attributes for Documents to use CDAM.")
                                .version("v1.0.1")
                );
    }
}
