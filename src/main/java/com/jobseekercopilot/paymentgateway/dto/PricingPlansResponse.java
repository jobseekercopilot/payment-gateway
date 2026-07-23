package com.jobseekercopilot.paymentgateway.dto;

import java.util.List;
import lombok.Data;

@Data
public class PricingPlansResponse {
    private List<TokenPricingPlanResponse> plans;
}
