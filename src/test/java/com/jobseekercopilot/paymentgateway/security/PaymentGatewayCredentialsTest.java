package com.jobseekercopilot.paymentgateway.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentGatewayCredentialsTest {
    private static final String BFF = "bff-payment-gateway-token-00000000000001";
    private static final String PAYMENT = "payment-gateway-service-token-00000000001";
    private static final String STRIPE = "payment-gateway-stripe-token-000000000001";

    @Test
    void requiresStrongDistinctTokens() {
        assertThrows(
                IllegalStateException.class,
                () -> new PaymentGatewayCredentials("short", PAYMENT, STRIPE));
        assertThrows(
                IllegalStateException.class,
                () -> new PaymentGatewayCredentials(BFF, BFF, STRIPE));
    }

    @Test
    void authenticatesOnlyTheConfiguredBffToken() {
        PaymentGatewayCredentials credentials =
                new PaymentGatewayCredentials(BFF, PAYMENT, STRIPE);

        assertTrue(credentials.authenticatesBff(BFF));
        assertFalse(credentials.authenticatesBff("forged"));
        assertFalse(credentials.authenticatesBff(null));
    }
}
