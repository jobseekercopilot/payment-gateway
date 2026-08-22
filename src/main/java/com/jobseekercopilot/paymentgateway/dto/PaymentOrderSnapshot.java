package com.jobseekercopilot.paymentgateway.dto;

import java.time.Instant;
import java.util.UUID;

public record PaymentOrderSnapshot(
        UUID orderId,
        String status,
        String ownerId,
        String catalogVersion,
        String pricingPlanId,
        String pricingPlanName,
        int documentCredits,
        int promotionBonusDocumentCredits,
        boolean promotionGuaranteed,
        long priceMinor,
        String currency,
        String billingCountry,
        String taxTreatment,
        String taxStatus,
        String legalEntityType,
        String legalEntityConfigurationVersion,
        boolean displayedPriceIsCheckoutTotal,
        String consumerTermsVersion,
        boolean consumerAcknowledgementsRecorded,
        Instant consumerTermsAcceptedAt,
        Instant expiresAt,
        String stripeSessionId) {}
