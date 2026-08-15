package com.jobseekercopilot.paymentgateway.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentOrderStatusResponse(
        UUID orderId,
        Status status,
        String pricingPlanId,
        int documentCredits,
        int promotionBonusDocumentCredits,
        int totalGrantedDocumentCredits,
        long priceMinor,
        String currency,
        String taxTreatment,
        String taxStatus,
        String legalEntityType,
        String legalEntityConfigurationVersion,
        Instant createdAt,
        Instant expiresAt,
        Instant fulfilledAt,
        boolean creditsAdded,
        MessageCode messageCode) {
    public enum Status {
        PENDING_CHECKOUT,
        CHECKOUT_OPEN,
        FULFILLED,
        EXPIRED,
        CANCELLED,
        REFUNDED,
        PARTIALLY_REFUNDED,
        DISPUTED,
        MANUAL_REVIEW
    }

    public enum MessageCode {
        PAYMENT_PENDING,
        CREDITS_ADDED,
        CHECKOUT_EXPIRED,
        CHECKOUT_CANCELLED,
        PAYMENT_REFUNDED,
        PAYMENT_PARTIALLY_REFUNDED,
        PAYMENT_DISPUTED,
        PAYMENT_REVIEW_REQUIRED
    }
}
