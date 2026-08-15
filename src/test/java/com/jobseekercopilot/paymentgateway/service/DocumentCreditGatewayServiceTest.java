package com.jobseekercopilot.paymentgateway.service;

import com.jobseekercopilot.paymentgateway.dto.DocumentCreditCheckoutRequest;
import com.jobseekercopilot.paymentgateway.dto.DocumentCreditCheckoutResponse;
import com.jobseekercopilot.paymentgateway.security.PaymentGatewayCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;

class DocumentCreditGatewayServiceTest {
    private static final String PAYMENT_TOKEN =
            "payment-gateway-service-test-token-000000001";
    private static final String STRIPE_TOKEN =
            "payment-gateway-stripe-test-token-0000000001";

    private MockRestServiceServer paymentServer;
    private MockRestServiceServer stripeServer;
    private DocumentCreditGatewayService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder payment = RestClient.builder().baseUrl("https://payment.example.test");
        RestClient.Builder stripe = RestClient.builder().baseUrl("https://stripe.example.test");
        paymentServer = MockRestServiceServer.bindTo(payment).build();
        stripeServer = MockRestServiceServer.bindTo(stripe).build();
        service = new DocumentCreditGatewayService(
                payment.build(), stripe.build(), new PaymentGatewayCredentials(
                "bff-payment-gateway-test-token-000000000001",
                PAYMENT_TOKEN, STRIPE_TOKEN));
    }

    @Test
    void checkoutCreatesDurableOwnedOrderBeforeProviderSession() {
        String orderId = "1c05d1ab-e57b-4904-b627-e55a7132207c";
        paymentServer.expect(requestTo("https://payment.example.test/api/v2/payments/orders"))
                .andExpect(header("X-Service-Token", PAYMENT_TOKEN))
                .andExpect(header("X-Payment-Owner", "owner-123"))
                .andExpect(header("Idempotency-Key", "click-123"))
                .andExpect(content().json("""
                        {"pricingPlanId":"active","billingCountry":"GB",
                         "immediateSupplyRequested":true,
                         "cancellationRightLossAcknowledged":true}
                        """))
                .andRespond(withSuccess("""
                        {"orderId":"%s","status":"PENDING_CHECKOUT","ownerId":"owner-123",
                         "catalogVersion":"public-beta-2026-08-15","pricingPlanId":"active",
                         "pricingPlanName":"Active","documentCredits":25,
                         "promotionBonusDocumentCredits":13,"promotionGuaranteed":true,
                         "priceMinor":1699,"currency":"GBP","billingCountry":"GB",
                         "taxTreatment":"VAT_NOT_CHARGED","taxStatus":"NOT_VAT_REGISTERED",
                         "legalEntityType":"SOLE_TRADER",
                         "legalEntityConfigurationVersion":"seller-terms-v1",
                         "displayedPriceIsCheckoutTotal":true,
                         "consumerTermsVersion":"uk-consumer-terms-2026-08-15",
                         "consumerAcknowledgementsRecorded":true,
                         "expiresAt":"2026-08-15T12:30:00Z"}
                        """.formatted(orderId), MediaType.APPLICATION_JSON));
        stripeServer.expect(requestTo("https://stripe.example.test/api/v2/stripe/checkout-sessions"))
                .andExpect(header("X-Service-Token", STRIPE_TOKEN))
                .andExpect(header("X-Payment-Owner", "owner-123"))
                .andExpect(header("Idempotency-Key", "click-123"))
                .andExpect(content().json("{\"orderId\":\"" + orderId + "\"}"))
                .andRespond(withSuccess("""
                        {"orderId":"%s","sessionId":"cs_test_owned",
                         "url":"https://checkout.stripe.test/cs_test_owned",
                         "expiresAt":"2026-08-15T12:30:00Z",
                         "promotionBonusDocumentCredits":13,"promotionGuaranteed":true}
                        """.formatted(orderId), MediaType.APPLICATION_JSON));

        DocumentCreditCheckoutResponse response = service.checkout(
                "owner-123", "click-123",
                new DocumentCreditCheckoutRequest("active", "GB", true, true));

        assertThat(response.url()).isEqualTo("https://checkout.stripe.test/cs_test_owned");
        assertThat(response.pricingSnapshot().documentCredits()).isEqualTo(25);
        assertThat(response.pricingSnapshot().taxStatus()).isEqualTo("NOT_VAT_REGISTERED");
        assertThat(response.promotionBonusDocumentCredits()).isEqualTo(13);
        assertThat(response.consumerAcknowledgementsRecorded()).isTrue();
        paymentServer.verify();
        stripeServer.verify();
    }

    @Test
    void ambiguousProviderFailureIsPropagatedWithoutBlindlyCancellingTheOrder() {
        String orderId = "1c05d1ab-e57b-4904-b627-e55a7132207c";
        paymentServer.expect(requestTo("https://payment.example.test/api/v2/payments/orders"))
                .andRespond(withSuccess("""
                        {"orderId":"%s","status":"PENDING_CHECKOUT","ownerId":"owner-123",
                         "catalogVersion":"public-beta-2026-08-15","pricingPlanId":"active",
                         "pricingPlanName":"Active","documentCredits":25,
                         "promotionBonusDocumentCredits":13,"promotionGuaranteed":true,
                         "priceMinor":1699,"currency":"GBP","billingCountry":"GB",
                         "taxTreatment":"VAT_NOT_CHARGED","taxStatus":"NOT_VAT_REGISTERED",
                         "legalEntityType":"SOLE_TRADER",
                         "legalEntityConfigurationVersion":"seller-terms-v1",
                         "displayedPriceIsCheckoutTotal":true,
                         "consumerTermsVersion":"uk-consumer-terms-2026-08-15",
                         "consumerAcknowledgementsRecorded":true,
                         "expiresAt":"2026-08-15T12:30:00Z"}
                        """.formatted(orderId), MediaType.APPLICATION_JSON));
        stripeServer.expect(requestTo(
                        "https://stripe.example.test/api/v2/stripe/checkout-sessions"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> service.checkout(
                        "owner-123",
                        "click-provider-ambiguous",
                        new DocumentCreditCheckoutRequest(
                                "active", "GB", true, true)))
                .isInstanceOf(org.springframework.web.client.RestClientException.class);

        // No Payment cancellation expectation exists: any guessed compensation
        // request would fail verification instead of silently passing.
        paymentServer.verify();
        stripeServer.verify();
    }
}
