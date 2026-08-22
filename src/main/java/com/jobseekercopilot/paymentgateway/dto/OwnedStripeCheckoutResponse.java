package com.jobseekercopilot.paymentgateway.dto;

import java.time.Instant;
import java.util.UUID;

public record OwnedStripeCheckoutResponse(
        UUID orderId,
        String sessionId,
        String url,
        Instant expiresAt,
        int promotionBonusDocumentCredits,
        boolean promotionGuaranteed) {}
