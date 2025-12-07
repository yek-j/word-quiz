package com.jyk.wordquiz.wordquiz.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    /**
     * Creates the OpenAPI specification for the application, including API metadata and JWT Bearer security.
     *
     * @return an OpenAPI instance configured with the API Info and a JWT Bearer security scheme named "bearerAuth"
     */
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("WordQuiz API Document")
                .version("v0.0.1")
                .description("단어장 API 명세서");

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement)
                .info(info);
    }
}