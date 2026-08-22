package com.jobseekercopilot.paymentgateway.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record DocumentCreditCatalogResponse(
        String catalogVersion,
        String currency,
        String billingCountry,
        String taxTreatment,
        String taxStatus,
        boolean displayedPriceIsCheckoutTotal,
        boolean automaticRenewal,
        @JsonProperty("generationUnit") @JsonAlias("creditUnit") String creditUnit,
        @JsonProperty("freeAllowanceGenerations") @JsonAlias("freeAllowanceCredits") int freeAllowanceCredits,
        List<Plan> plans,
        Promotion promotion) {
    public record Plan(
            String id,
            String name,
            String description,
            @JsonProperty("documentGenerations") @JsonAlias("documentCredits") int documentCredits,
            long priceMinor,
            String currency,
            int fullApplicationEquivalent,
            @JsonProperty("promotionBonusDocumentGenerations") @JsonAlias("promotionBonusDocumentCredits") int promotionBonusDocumentCredits,
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
