package com.jobseekercopilot.paymentgateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI paymentGatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Gateway API")
                        .description("Frontend-facing gateway for AI token wallet, pricing, demo purchase, and estimate APIs.")
                        .version("1.0.0")
                        .contact(new Contact().name("Jobseeker Copilot"))
                        .license(new License().name("MIT")));
    }
}
