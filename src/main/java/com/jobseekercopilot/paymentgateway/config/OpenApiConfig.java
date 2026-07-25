package com.jobseekercopilot.paymentgateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI paymentGatewayOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(
                        "serviceToken",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Service-Token")
                                .description("Dedicated Job Seeker Copilot BFF service identity.")))
                .info(new Info()
                        .title("Payment Gateway API")
                        .description("Frontend-facing gateway for AI token wallet, pricing, demo purchase, and estimate APIs.")
                        .version("2.0.0")
                        .contact(new Contact().name("Jobseeker Copilot"))
                        .license(new License().name("MIT")));
    }
}
