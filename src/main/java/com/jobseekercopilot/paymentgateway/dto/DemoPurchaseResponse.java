package com.jobseekercopilot.paymentgateway.dto;

import lombok.Data;

@Data
public class DemoPurchaseResponse {
    private WalletSummaryResponse wallet;
    private TransactionResponse transaction;
}
