package com.example.loanengine.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI loanEngineOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Loan Settlement & Prepayment Engine API")
                        .description("APIs to manage loans, schedules, prepayments and settlements")
                        .version("0.0.1")
                        .contact(new Contact().name("Dev Team"))
                        .license(new License().name("MIT"))
                );
    }
}
