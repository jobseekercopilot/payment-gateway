package com.jobseekercopilot.paymentgateway.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DemoPurchaseRequest {
    @NotBlank
    private String pricingPlanId;
}
