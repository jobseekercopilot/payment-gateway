package com.jobseekercopilot.paymentgateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record DocumentCreditCheckoutRequest(
        @NotBlank String pricingPlanId,
        @NotBlank @Pattern(regexp = "GB") String billingCountry,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "true")
                Boolean immediateSupplyRequested,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "true")
                Boolean cancellationRightLossAcknowledged) {}
