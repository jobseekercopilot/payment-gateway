package com.jobseekercopilot.paymentgateway.dto;

import lombok.Data;

@Data
public class WalletSummaryResponse {
    private String userId;
    private long balanceTokens;
    private long lifetimePurchasedTokens;
    private long lifetimeSpentTokens;
    private long lifetimeRefundedTokens;
    private boolean freeTrialGranted;
}
