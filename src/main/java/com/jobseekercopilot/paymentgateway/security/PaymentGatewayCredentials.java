package com.jobseekercopilot.paymentgateway.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class PaymentGatewayCredentials {
    static final int MINIMUM_TOKEN_BYTES = 32;

    private final String bffToken;
    private final String paymentServiceToken;
    private final String stripeGatewayToken;

    public PaymentGatewayCredentials(
            @Value("${payment.security.bff-token}") String bffToken,
            @Value("${payment.security.payment-service-token}") String paymentServiceToken,
            @Value("${payment.security.stripe-gateway-token}") String stripeGatewayToken) {
        this.bffToken = validate(bffToken, "BFF service token");
        this.paymentServiceToken = validate(paymentServiceToken, "Payment Service token");
        this.stripeGatewayToken = validate(stripeGatewayToken, "Stripe Gateway token");
        if (matches(this.bffToken, this.paymentServiceToken)
                || matches(this.bffToken, this.stripeGatewayToken)
                || matches(this.paymentServiceToken, this.stripeGatewayToken)) {
            throw new IllegalStateException("Payment Gateway service identity tokens must be distinct.");
        }
    }

    public boolean authenticatesBff(String supplied) {
        return matches(supplied, bffToken);
    }

    public String paymentServiceToken() {
        return paymentServiceToken;
    }

    public String stripeGatewayToken() {
        return stripeGatewayToken;
    }

    private static String validate(String value, String label) {
        if (value == null
                || value.isBlank()
                || value.getBytes(StandardCharsets.UTF_8).length < MINIMUM_TOKEN_BYTES) {
            throw new IllegalStateException(label + " must contain at least 32 bytes.");
        }
        return value;
    }

    private static boolean matches(String supplied, String expected) {
        return supplied != null && MessageDigest.isEqual(
                supplied.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }
}
