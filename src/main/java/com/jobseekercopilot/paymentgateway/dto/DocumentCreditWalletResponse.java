package com.jobseekercopilot.paymentgateway.dto;

public record DocumentCreditWalletResponse(
        int balanceDocumentCredits,
        int lifetimePurchasedDocumentCredits,
        int lifetimeSpentDocumentCredits,
        int lifetimeReversedDocumentCredits,
        int reviewDebtDocumentCredits,
        boolean freeAllowanceGranted,
        String status) {}
