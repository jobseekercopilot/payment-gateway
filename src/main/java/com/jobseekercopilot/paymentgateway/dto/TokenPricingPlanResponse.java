package com.jobseekercopilot.paymentgateway.dto;

import lombok.Data;

@Data
public class TokenPricingPlanResponse {
    private String id;
    private String name;
    private String description;
    private long tokenAmount;
    private long priceGbpPence;
}
