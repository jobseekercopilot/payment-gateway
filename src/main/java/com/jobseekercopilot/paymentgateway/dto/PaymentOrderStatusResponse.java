package com.jobseekercopilot.paymentgateway.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import java.time.Instant;
import java.util.UUID;

public record PaymentOrderStatusResponse(
        UUID orderId,
        Status status,
        String pricingPlanId,
        @JsonProperty("documentGenerations") @JsonAlias("documentCredits") int documentCredits,
        @JsonProperty("promotionBonusDocumentGenerations") @JsonAlias("promotionBonusDocumentCredits") int promotionBonusDocumentCredits,
        @JsonProperty("totalGrantedDocumentGenerations") @JsonAlias("totalGrantedDocumentCredits") int totalGrantedDocumentCredits,
        long priceMinor,
        String currency,
        String taxTreatment,
        String taxStatus,
        String legalEntityType,
        String legalEntityConfigurationVersion,
        Instant createdAt,
        Instant expiresAt,
        Instant fulfilledAt,
        @JsonProperty("generationsAdded") @JsonAlias("creditsAdded") boolean creditsAdded,
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
        PAYMENT_REVIEW_REQUIRED;

        @JsonCreator
        public static MessageCode fromJson(String value) {
            if ("GENERATIONS_ADDED".equals(value)) return CREDITS_ADDED;
            return valueOf(value);
        }

        @JsonValue
        public String toJson() {
            return this == CREDITS_ADDED ? "GENERATIONS_ADDED" : name();
        }
    }
}
