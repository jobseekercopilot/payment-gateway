package com.jobseekercopilot.paymentgateway.config;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PaymentGatewayProductionConfigurationVerifier
        implements ApplicationRunner {
    private final Environment environment;
    private final String paymentServiceUrl;
    private final String stripeGatewayUrl;

    public PaymentGatewayProductionConfigurationVerifier(
            Environment environment,
            @Value("${services.payment-service.base-url}")
            String paymentServiceUrl,
            @Value("${services.stripe-gateway.base-url}")
            String stripeGatewayUrl) {
        this.environment = environment;
        this.paymentServiceUrl = paymentServiceUrl;
        this.stripeGatewayUrl = stripeGatewayUrl;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!production()) {
            return;
        }
        List<String> missing = List.of(
                        "PAYMENT_SERVICE_URL",
                        "STRIPE_GATEWAY_URL")
                .stream()
                .filter(name -> {
                    String value = environment.getProperty(name);
                    return value == null || value.isBlank();
                })
                .toList();
        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Production payment-gateway URLs must be explicit: "
                            + String.join(", ", missing));
        }
        requireInternalServiceUrl(paymentServiceUrl, "Payment Service");
        requireInternalServiceUrl(stripeGatewayUrl, "Stripe Gateway");
    }

    private void requireInternalServiceUrl(String value, String label) {
        try {
            URI uri = URI.create(value);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (!("http".equalsIgnoreCase(scheme)
                    || "https".equalsIgnoreCase(scheme))
                    || host == null
                    || host.equalsIgnoreCase("localhost")
                    || host.equals("127.0.0.1")
                    || host.equals("::1")
                    || uri.getUserInfo() != null
                    || uri.getQuery() != null
                    || uri.getFragment() != null) {
                throw new IllegalArgumentException();
            }
        } catch (RuntimeException invalid) {
            throw new IllegalStateException(
                    label
                            + " production URL must be an absolute internal HTTP(S) base URL.");
        }
    }

    private boolean production() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("prod")
                        || profile.equalsIgnoreCase("production"));
    }
}
