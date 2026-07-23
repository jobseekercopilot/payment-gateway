package com.jobseekercopilot.paymentgateway.dto;

import java.util.List;
import lombok.Data;

@Data
public class TransactionsResponse {
    private String userId;
    private List<TransactionResponse> transactions;
}
