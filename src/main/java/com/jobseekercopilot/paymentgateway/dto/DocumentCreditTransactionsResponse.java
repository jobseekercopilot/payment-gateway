package com.jobseekercopilot.paymentgateway.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DocumentCreditTransactionsResponse(List<Transaction> transactions) {
    public record Transaction(
            UUID id,
            Type type,
            int documentCredits,
            int balanceBeforeDocumentCredits,
            int balanceAfterDocumentCredits,
            String operationId,
            String description,
            String referenceType,
            String referenceId,
            Instant createdAt) {}

    public enum Type {
        FREE_ALLOWANCE_GRANTED,
        PURCHASE,
        PROMOTION_BONUS,
        DOCUMENT_RESERVED,
        DOCUMENT_SPENT,
        DOCUMENT_RESERVATION_RELEASED,
        REFUND_REVERSAL,
        DISPUTE_REVERSAL,
        ADJUSTMENT
    }
}
