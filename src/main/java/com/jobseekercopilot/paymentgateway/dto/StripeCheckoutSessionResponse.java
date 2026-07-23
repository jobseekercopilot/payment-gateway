package com.jobseekercopilot.paymentgateway.dto;

import lombok.Data;

@Data
public class StripeCheckoutSessionResponse {
    private String sessionId;
    private String checkoutUrl;
}
