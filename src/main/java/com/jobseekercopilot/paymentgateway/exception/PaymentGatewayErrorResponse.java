package com.jobseekercopilot.paymentgateway.exception;

import io.swagger.v3.oas.annotations.media.Schema;

public record PaymentGatewayErrorResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String error,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String code,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String message) {}
