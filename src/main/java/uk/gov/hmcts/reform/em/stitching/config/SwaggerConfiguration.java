package uk.gov.hmcts.reform.em.stitching.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@ComponentScan("uk.gov.hmcts.reform.em.stitching.rest")
public class SwaggerConfiguration {

    private final static String apiVersion = "0.0.1";

    private static final String MODEL_REF_TYPE = "string";
    private static final String PARAMETER_TYPE = "header";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.regex("/api(.*)"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("EM Native PDF Annotator App")
                .description("API to burn annotations onto a PDF")
                .version(apiVersion)
                .build();
    }
}
