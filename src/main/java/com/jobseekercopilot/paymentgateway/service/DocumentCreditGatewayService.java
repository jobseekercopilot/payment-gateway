package com.jobseekercopilot.paymentgateway.service;

import com.jobseekercopilot.paymentgateway.dto.CheckoutReadinessResponse;
import com.jobseekercopilot.paymentgateway.dto.DocumentCreditCatalogResponse;
import com.jobseekercopilot.paymentgateway.dto.DocumentCreditCheckoutRequest;
import com.jobseekercopilot.paymentgateway.dto.DocumentCreditCheckoutResponse;
import com.jobseekercopilot.paymentgateway.dto.DocumentCreditTransactionsResponse;
import com.jobseekercopilot.paymentgateway.dto.DocumentCreditWalletResponse;
import com.jobseekercopilot.paymentgateway.dto.DownstreamReadinessResponse;
import com.jobseekercopilot.paymentgateway.dto.OwnedStripeCheckoutResponse;
import com.jobseekercopilot.paymentgateway.dto.PaymentOrderSnapshot;
import com.jobseekercopilot.paymentgateway.dto.PaymentOrderStatusResponse;
import com.jobseekercopilot.paymentgateway.security.PaymentGatewayCredentials;
import com.jobseekercopilot.paymentgateway.exception.PaymentGatewayApiException;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class DocumentCreditGatewayService {
    private static final Logger log = LoggerFactory.getLogger(DocumentCreditGatewayService.class);
    private static final String SERVICE_TOKEN = "X-Service-Token";
    private static final String OWNER = "X-Payment-Owner";
    private static final String IDEMPOTENCY_KEY = "Idempotency-Key";

    private final RestClient paymentService;
    private final RestClient stripeGateway;
    private final PaymentGatewayCredentials credentials;

    public DocumentCreditGatewayService(
            RestClient paymentServiceRestClient,
            @Qualifier("stripeGatewayRestClient") RestClient stripeGatewayRestClient,
            PaymentGatewayCredentials credentials) {
        this.paymentService = paymentServiceRestClient;
        this.stripeGateway = stripeGatewayRestClient;
        this.credentials = credentials;
    }

    public DocumentCreditCatalogResponse catalog(String owner) {
        return paymentService.get().uri("/api/v2/payments/catalog")
                .headers(headers -> paymentHeaders(headers, owner))
                .retrieve().body(DocumentCreditCatalogResponse.class);
    }

    public DocumentCreditWalletResponse wallet(String owner) {
        return paymentService.get().uri("/api/v2/payments/wallet")
                .headers(headers -> paymentHeaders(headers, owner))
                .retrieve().body(DocumentCreditWalletResponse.class);
    }

    public DocumentCreditTransactionsResponse transactions(String owner, int limit) {
        return paymentService.get().uri(builder -> builder
                        .path("/api/v2/payments/transactions")
                        .queryParam("limit", Math.max(1, Math.min(limit, 100))).build())
                .headers(headers -> paymentHeaders(headers, owner))
                .retrieve().body(DocumentCreditTransactionsResponse.class);
    }

    public CheckoutReadinessResponse readiness(String owner) {
        DownstreamReadinessResponse payment;
        try {
            payment = paymentService.get().uri("/api/v2/payments/checkout-readiness")
                    .headers(headers -> paymentHeaders(headers, owner))
                    .retrieve().body(DownstreamReadinessResponse.class);
        } catch (RestClientException failure) {
            return new CheckoutReadinessResponse(false, "PAYMENT_SERVICE_UNAVAILABLE",
                    "UNAVAILABLE", "NOT_CHECKED", "DISABLED");
        }
        if (payment == null || !payment.checkoutAvailable()) {
            return new CheckoutReadinessResponse(false,
                    payment == null ? "PAYMENT_SERVICE_UNAVAILABLE" : payment.code(),
                    payment == null ? "UNAVAILABLE" : payment.code(),
                    "NOT_CHECKED", payment == null ? "DISABLED" : payment.mode());
        }
        DownstreamReadinessResponse provider;
        try {
            provider = stripeGateway.get().uri("/api/v2/stripe/readiness")
                    .header(SERVICE_TOKEN, credentials.stripeGatewayToken())
                    .retrieve().body(DownstreamReadinessResponse.class);
        } catch (RestClientException failure) {
            return new CheckoutReadinessResponse(false, "PAYMENT_PROVIDER_UNAVAILABLE",
                    payment.code(), "UNAVAILABLE", payment.mode());
        }
        boolean ready = provider != null && provider.checkoutAvailable();
        return new CheckoutReadinessResponse(
                ready,
                ready ? "READY" : provider == null ? "PAYMENT_PROVIDER_UNAVAILABLE" : provider.code(),
                payment.code(),
                provider == null ? "UNAVAILABLE" : provider.code(),
                provider == null ? payment.mode() : provider.mode());
    }

    public DocumentCreditCheckoutResponse checkout(
            String owner,
            String idempotencyKey,
            DocumentCreditCheckoutRequest request) {
        String key = requireIdempotencyKey(idempotencyKey);
        if (!Boolean.TRUE.equals(request.immediateSupplyRequested())
                || !Boolean.TRUE.equals(request.cancellationRightLossAcknowledged())) {
            throw new PaymentGatewayApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "CONSUMER_ACKNOWLEDGEMENTS_REQUIRED",
                    "Checkout requires express immediate-supply and cancellation-right acknowledgements.");
        }
        PaymentOrderSnapshot order = paymentService.post().uri("/api/v2/payments/orders")
                .headers(headers -> {
                    paymentHeaders(headers, owner);
                    headers.set(IDEMPOTENCY_KEY, key);
                })
                .body(request)
                .retrieve().body(PaymentOrderSnapshot.class);
        if (order == null || order.orderId() == null) {
            throw new IllegalStateException("Payment Service returned an incomplete order");
        }
        OwnedStripeCheckoutResponse checkout;
        try {
            checkout = stripeGateway.post().uri("/api/v2/stripe/checkout-sessions")
                    .header(SERVICE_TOKEN, credentials.stripeGatewayToken())
                    .header(OWNER, owner)
                    .header(IDEMPOTENCY_KEY, key)
                    .body(Map.of("orderId", order.orderId()))
                    .retrieve().body(OwnedStripeCheckoutResponse.class);
        } catch (RestClientException failure) {
            cancelUnboundOrderBestEffort(owner, order.orderId());
            throw failure;
        }
        if (checkout == null
                || !order.orderId().equals(checkout.orderId())
                || checkout.url() == null
                || checkout.url().isBlank()) {
            cancelUnboundOrderBestEffort(owner, order.orderId());
            throw new IllegalStateException("Payment provider returned an incomplete Checkout session");
        }
        return new DocumentCreditCheckoutResponse(
                order.orderId(),
                checkout.sessionId(),
                checkout.url(),
                DocumentCreditCheckoutResponse.Status.CHECKOUT_OPEN,
                checkout.expiresAt(),
                new DocumentCreditCheckoutResponse.PricingSnapshot(
                        order.catalogVersion(), order.pricingPlanId(), order.pricingPlanName(),
                        order.documentCredits(), order.priceMinor(), order.currency(),
                        order.billingCountry(), order.taxTreatment(),
                        order.taxStatus(), order.legalEntityType(),
                        order.legalEntityConfigurationVersion(),
                        order.displayedPriceIsCheckoutTotal()),
                checkout.promotionBonusDocumentCredits(),
                checkout.promotionGuaranteed(),
                order.consumerTermsVersion(),
                order.consumerAcknowledgementsRecorded());
    }

    private String requireIdempotencyKey(String value) {
        if (value == null || value.isBlank()) {
            throw new PaymentGatewayApiException(HttpStatus.BAD_REQUEST,
                    "IDEMPOTENCY_KEY_REQUIRED", "Idempotency-Key is required for Checkout.");
        }
        String key = value.trim();
        if (key.length() > 128 || !key.matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,127}")) {
            throw new PaymentGatewayApiException(HttpStatus.BAD_REQUEST,
                    "IDEMPOTENCY_KEY_INVALID", "Idempotency-Key has an invalid format.");
        }
        return key;
    }

    public PaymentOrderStatusResponse orderStatus(String owner, UUID orderId) {
        return paymentService.get()
                .uri("/api/v2/payments/orders/{orderId}/status", orderId)
                .headers(headers -> paymentHeaders(headers, owner))
                .retrieve().body(PaymentOrderStatusResponse.class);
    }

    private void cancelUnboundOrderBestEffort(String owner, UUID orderId) {
        try {
            paymentService.post().uri("/api/v2/payments/orders/{orderId}/cancel", orderId)
                    .headers(headers -> paymentHeaders(headers, owner))
                    .retrieve().toBodilessEntity();
        } catch (RestClientException cancellationFailure) {
            log.warn("Checkout setup failed and order could not be locally cancelled orderId={} error={}",
                    orderId, cancellationFailure.getClass().getSimpleName());
        }
    }

    private void paymentHeaders(org.springframework.http.HttpHeaders headers, String owner) {
        headers.set(SERVICE_TOKEN, credentials.paymentServiceToken());
        headers.set(OWNER, owner);
    }
}
