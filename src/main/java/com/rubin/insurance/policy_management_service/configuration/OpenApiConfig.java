package com.rubin.insurance.policy_management_service.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Insurance Policy Management API")
                        .version("1.0")
                        .description("REST API for managing insurance policies and claims. " +
                                "Create and manage policies (health, auto, home, life), renew or cancel them, " +
                                "and submit and track claims with status updates.")
                        .contact(new Contact()
                                .name("Insurance Policy Management System"))
                        .license(new License().name("Unlicensed")));
    }
}
