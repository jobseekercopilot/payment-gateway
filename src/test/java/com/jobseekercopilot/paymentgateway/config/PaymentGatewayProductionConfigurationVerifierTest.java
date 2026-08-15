package com.jobseekercopilot.paymentgateway.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class PaymentGatewayProductionConfigurationVerifierTest {
    @Test
    void productionRequiresExplicitNonLocalServiceUrls() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("production");
        environment.setProperty("PAYMENT_SERVICE_URL", "http://localhost:8099");
        environment.setProperty("STRIPE_GATEWAY_URL", "http://stripe.service:8100");

        PaymentGatewayProductionConfigurationVerifier verifier =
                new PaymentGatewayProductionConfigurationVerifier(
                        environment,
                        "http://localhost:8099",
                        "http://stripe.service:8100");

        assertThrows(IllegalStateException.class, () -> verifier.run(null));
    }

    @Test
    void productionAcceptsExplicitInternalServiceDiscoveryUrls() {
        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("prod");
        environment.setProperty(
                "PAYMENT_SERVICE_URL", "http://payment.service:8099");
        environment.setProperty(
                "STRIPE_GATEWAY_URL", "http://stripe.service:8100");

        PaymentGatewayProductionConfigurationVerifier verifier =
                new PaymentGatewayProductionConfigurationVerifier(
                        environment,
                        "http://payment.service:8099",
                        "http://stripe.service:8100");

        assertDoesNotThrow(() -> verifier.run(null));
    }

    @Test
    void localAndTestProfilesKeepDeveloperDefaults() {
        MockEnvironment environment = new MockEnvironment();
        PaymentGatewayProductionConfigurationVerifier verifier =
                new PaymentGatewayProductionConfigurationVerifier(
                        environment,
                        "http://localhost:8099",
                        "http://localhost:8100");

        assertDoesNotThrow(() -> verifier.run(null));
    }
}
