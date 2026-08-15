package com.jobseekercopilot.paymentgateway.dto;

public record DownstreamReadinessResponse(
        boolean checkoutAvailable,
        String code,
        String mode) {}
