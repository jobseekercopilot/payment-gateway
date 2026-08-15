package com.jobseekercopilot.paymentgateway.dto;

public record CheckoutReadinessResponse(
        boolean checkoutAvailable,
        String code,
        String paymentServiceCode,
        String providerCode,
        String mode) {}
