package com.jobseekercopilot.paymentgateway.service;

import com.jobseekercopilot.paymentgateway.dto.CheckoutRequest;
import com.jobseekercopilot.paymentgateway.dto.CheckoutResponse;
import com.jobseekercopilot.paymentgateway.dto.DemoPurchaseRequest;
import com.jobseekercopilot.paymentgateway.dto.DemoPurchaseResponse;
import com.jobseekercopilot.paymentgateway.dto.EstimateRequest;
import com.jobseekercopilot.paymentgateway.dto.EstimateResponse;
import com.jobseekercopilot.paymentgateway.dto.PricingPlansResponse;
import com.jobseekercopilot.paymentgateway.dto.StripeCheckoutSessionRequest;
import com.jobseekercopilot.paymentgateway.dto.StripeCheckoutSessionResponse;
import com.jobseekercopilot.paymentgateway.dto.TokenPricingPlanResponse;
import com.jobseekercopilot.paymentgateway.dto.TransactionsResponse;
import com.jobseekercopilot.paymentgateway.dto.WalletSummaryResponse;
import com.jobseekercopilot.paymentgateway.exception.BadRequestException;
import com.jobseekercopilot.paymentgateway.security.PaymentGatewayCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PaymentGatewayService {
    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayService.class);
    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";
    private static final String OWNER_HEADER = "X-Payment-Owner";
    private final RestClient paymentServiceRestClient;
    private final RestClient stripeGatewayRestClient;
    private final PaymentGatewayCredentials credentials;

    public PaymentGatewayService(
            RestClient paymentServiceRestClient,
            @Qualifier("stripeGatewayRestClient") RestClient stripeGatewayRestClient,
            PaymentGatewayCredentials credentials) {
        this.paymentServiceRestClient = paymentServiceRestClient;
        this.stripeGatewayRestClient = stripeGatewayRestClient;
        this.credentials = credentials;
    }

    public WalletSummaryResponse wallet(String userId) {
        long startedAt = System.nanoTime();
        log.info("payment-gateway wallet request userId={}", userId);
        WalletSummaryResponse response = paymentServiceRestClient.get()
                .uri("/api/v1/payments/wallet")
                .header(SERVICE_TOKEN_HEADER, credentials.paymentServiceToken())
                .header(OWNER_HEADER, userId)
                .retrieve()
                .body(WalletSummaryResponse.class);
        log.info("payment-service wallet returned userId={} durationMs={}",
                userId,
                (System.nanoTime() - startedAt) / 1_000_000);
        return response;
    }

    public TransactionsResponse transactions(String userId, int limit) {
        return paymentServiceRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/payments/transactions")
                        .queryParam("limit", limit)
                        .build())
                .header(SERVICE_TOKEN_HEADER, credentials.paymentServiceToken())
                .header(OWNER_HEADER, userId)
                .retrieve()
                .body(TransactionsResponse.class);
    }

    public PricingPlansResponse pricing() {
        long startedAt = System.nanoTime();
        log.info("payment-gateway pricing request");
        PricingPlansResponse response = paymentServiceRestClient.get()
                .uri("/api/v1/payments/pricing")
                .header(SERVICE_TOKEN_HEADER, credentials.paymentServiceToken())
                .retrieve()
                .body(PricingPlansResponse.class);
        log.info("payment-service pricing returned plans={} durationMs={}",
                response == null || response.getPlans() == null ? 0 : response.getPlans().size(),
                (System.nanoTime() - startedAt) / 1_000_000);
        return response;
    }

    public DemoPurchaseResponse demoPurchase(String userId, DemoPurchaseRequest request) {
        long startedAt = System.nanoTime();
        log.info("payment-gateway demo purchase request userId={} pricingPlanId={}", userId, request.getPricingPlanId());
        DemoPurchaseResponse response = paymentServiceRestClient.post()
                .uri("/api/v1/payments/demo-purchase")
                .header(SERVICE_TOKEN_HEADER, credentials.paymentServiceToken())
                .header(OWNER_HEADER, userId)
                .body(request)
                .retrieve()
                .body(DemoPurchaseResponse.class);
        log.info("payment-service demo purchase returned userId={} durationMs={}",
                userId,
                (System.nanoTime() - startedAt) / 1_000_000);
        return response;
    }

    public CheckoutResponse checkout(String userId, CheckoutRequest request) {
        long startedAt = System.nanoTime();
        log.info("payment-gateway checkout request userId={} pricingPlanId={}", userId, request.getPricingPlanId());
        TokenPricingPlanResponse plan = pricing().getPlans().stream()
                .filter(candidate -> candidate.getId().equals(request.getPricingPlanId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unknown pricing plan: " + request.getPricingPlanId()));
        StripeCheckoutSessionRequest stripeRequest = StripeCheckoutSessionRequest.builder()
                .userId(userId)
                .pricingPlanId(plan.getId())
                .tokenAmount(plan.getTokenAmount())
                .priceGbpPence(plan.getPriceGbpPence())
                .build();
        StripeCheckoutSessionResponse stripeResponse = stripeGatewayRestClient.post()
                .uri("/api/v1/stripe/checkout-sessions")
                .header(SERVICE_TOKEN_HEADER, credentials.stripeGatewayToken())
                .header(OWNER_HEADER, userId)
                .body(stripeRequest)
                .retrieve()
                .body(StripeCheckoutSessionResponse.class);
        log.info("stripe-gateway checkout session returned userId={} pricingPlanId={} hasSession={} durationMs={}",
                userId,
                plan.getId(),
                stripeResponse != null && stripeResponse.getSessionId() != null,
                (System.nanoTime() - startedAt) / 1_000_000);
        CheckoutResponse response = new CheckoutResponse();
        if (stripeResponse != null) {
            response.setSessionId(stripeResponse.getSessionId());
            response.setCheckoutUrl(stripeResponse.getCheckoutUrl());
        }
        return response;
    }

    public EstimateResponse estimate(String userId, EstimateRequest request) {
        long startedAt = System.nanoTime();
        log.info("payment-gateway estimate request userId={} feature={}", userId, request.getFeature());
        EstimateResponse response = paymentServiceRestClient.post()
                .uri("/api/v1/payments/estimate")
                .header(SERVICE_TOKEN_HEADER, credentials.paymentServiceToken())
                .header(OWNER_HEADER, userId)
                .body(request)
                .retrieve()
                .body(EstimateResponse.class);
        log.info("payment-service estimate returned userId={} canAfford={} durationMs={}",
                userId,
                response == null ? null : response.isCanAfford(),
                (System.nanoTime() - startedAt) / 1_000_000);
        return response;
    }
}
