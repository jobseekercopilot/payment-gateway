package com.jobseekercopilot.paymentgateway.dto;

import java.time.Instant;
import java.util.UUID;

public record DocumentCreditCheckoutResponse(
        UUID orderId,
        String checkoutSessionId,
        String url,
        Status status,
        Instant expiresAt,
        PricingSnapshot pricingSnapshot,
        int promotionBonusDocumentCredits,
        boolean promotionGuaranteed,
        String consumerTermsVersion,
        boolean consumerAcknowledgementsRecorded) {
    public enum Status {
        CHECKOUT_OPEN
    }

    public record PricingSnapshot(
            String catalogVersion,
            String pricingPlanId,
            String pricingPlanName,
            int documentCredits,
            long priceMinor,
            String currency,
            String billingCountry,
            String taxTreatment,
            String taxStatus,
            String legalEntityType,
            String legalEntityConfigurationVersion,
            boolean displayedPriceIsCheckoutTotal) {}
}
