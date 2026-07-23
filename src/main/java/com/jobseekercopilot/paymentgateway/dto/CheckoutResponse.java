package com.jobseekercopilot.paymentgateway.dto;

import lombok.Data;

@Data
public class CheckoutResponse {
    private String sessionId;
    private String checkoutUrl;
}
