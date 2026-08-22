package com.jobseekercopilot.paymentgateway.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record DocumentCreditWalletResponse(
        @JsonProperty("remainingDocumentGenerations") @JsonAlias("balanceDocumentCredits") int balanceDocumentCredits,
        @JsonProperty("lifetimePurchasedDocumentGenerations") @JsonAlias("lifetimePurchasedDocumentCredits") int lifetimePurchasedDocumentCredits,
        @JsonProperty("lifetimeUsedDocumentGenerations") @JsonAlias("lifetimeSpentDocumentCredits") int lifetimeSpentDocumentCredits,
        @JsonProperty("lifetimeReversedDocumentGenerations") @JsonAlias("lifetimeReversedDocumentCredits") int lifetimeReversedDocumentCredits,
        @JsonProperty("reviewDebtDocumentGenerations") @JsonAlias("reviewDebtDocumentCredits") int reviewDebtDocumentCredits,
        boolean freeAllowanceGranted,
        String status) {}
