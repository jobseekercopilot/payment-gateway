package com.jobseekercopilot.paymentgateway.dto;

import java.util.List;

public record DocumentCreditCatalogResponse(
        String catalogVersion,
        String currency,
        String billingCountry,
        String taxTreatment,
        String taxStatus,
        boolean displayedPriceIsCheckoutTotal,
        boolean automaticRenewal,
        String creditUnit,
        int freeAllowanceCredits,
        List<Plan> plans,
        Promotion promotion) {
    public record Plan(
            String id,
            String name,
            String description,
            int documentCredits,
            long priceMinor,
            String currency,
            int fullApplicationEquivalent,
            int promotionBonusDocumentCredits,
            boolean active,
            int sortOrder) {}
    public record Promotion(
            String id,
            boolean enabled,
            Status status,
            int bonusPercent,
            int customerLimit) {
        public enum Status {
            DISABLED,
            EXHAUSTED,
            AVAILABLE
        }
    }
}
