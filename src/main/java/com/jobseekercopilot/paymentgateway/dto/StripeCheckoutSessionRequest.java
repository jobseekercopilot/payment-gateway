package com.jobseekercopilot.paymentgateway.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StripeCheckoutSessionRequest {
    private String userId;
    private String pricingPlanId;
    private long tokenAmount;
    private long priceGbpPence;
}
