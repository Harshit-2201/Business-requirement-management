package com.passwordmanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI passwordManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Zero-Knowledge Password Manager REST API")
                        .description("High-security backend for Zero-Knowledge Password Vault. " +
                                "Master passwords and raw credentials are never transmitted in plaintext. " +
                                "Built with Spring Boot 3, Spring Security, JWT, AES-256-GCM, and Argon2id.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("API Security Team")
                                .email("security@passwordmanager.local"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token obtained from /auth/login or /auth/register.")));
    }
}
