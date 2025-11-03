package com.example.lms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for the LMS application.
 * Configures Bearer token authentication for API endpoints.
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configures OpenAPI documentation with Bearer token authentication.
     * This adds an "Authorize" button to Swagger UI where users can enter their bearer token.
     * 
     * @return Configured OpenAPI instance
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter your bearer token (without 'Bearer ' prefix)")))
                .info(new Info()
                        .title("LMS API")
                        .version("v1.0")
                        .description("Learning Management System API with SharePoint/OneDrive integration"));
    }
}
