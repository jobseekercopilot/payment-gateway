package com.jobseekercopilot.paymentgateway.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class TransactionResponse {
    private UUID id;
    private String transactionType;
    private long tokenAmount;
    private long balanceBefore;
    private long balanceAfter;
    private String description;
    private String referenceType;
    private String referenceId;
    private LocalDateTime createdAt;
}
