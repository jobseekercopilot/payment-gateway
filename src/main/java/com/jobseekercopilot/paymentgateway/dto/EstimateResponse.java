package com.jobseekercopilot.paymentgateway.dto;

import lombok.Data;

@Data
public class EstimateResponse {
    private String feature;
    private long estimatedTotalTokens;
    private long estimatedCostTokens;
    private long userBalanceTokens;
    private boolean canAfford;
}
