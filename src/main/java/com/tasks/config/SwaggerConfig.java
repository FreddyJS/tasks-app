package com.tasks.config;

import java.security.Principal;
import java.util.Arrays;
import javax.servlet.ServletContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.service.Tag;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    
    @Bean
    public Docket api(ServletContext servletContext){
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(PathSelectors.regex("/api/.*"))
            .build()
            .securitySchemes(Arrays.asList(apiKey()))
            .apiInfo(apiInfo()).useDefaultResponseMessages(false)
            .ignoredParameterTypes(Principal.class)
            .tags(new Tag("Projects Management", "Projects Management REST API"),
                  new Tag("Tasks Management", "Tasks Management REST API"),
                  new Tag("Authentication", "Authentication REST API"));
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
            .title("Project Management")
            .description("Project Management REST service API Documentation")
            .build();
    }

    private ApiKey apiKey() {
        return new ApiKey("Bearer", "Authorization", "header");
    }

}
