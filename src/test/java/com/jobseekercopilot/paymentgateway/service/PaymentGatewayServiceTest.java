package com.jobseekercopilot.paymentgateway.service;

import com.jobseekercopilot.paymentgateway.dto.CheckoutRequest;
import com.jobseekercopilot.paymentgateway.security.PaymentGatewayCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PaymentGatewayServiceTest {
    private static final String PAYMENT_TOKEN =
            "payment-gateway-service-test-token-000000001";
    private static final String STRIPE_TOKEN =
            "payment-gateway-stripe-test-token-0000000001";

    private MockRestServiceServer paymentServer;
    private MockRestServiceServer stripeServer;
    private PaymentGatewayService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder paymentBuilder =
                RestClient.builder().baseUrl("https://payment.example.test");
        RestClient.Builder stripeBuilder =
                RestClient.builder().baseUrl("https://stripe.example.test");
        paymentServer = MockRestServiceServer.bindTo(paymentBuilder).build();
        stripeServer = MockRestServiceServer.bindTo(stripeBuilder).build();
        service = new PaymentGatewayService(
                paymentBuilder.build(),
                stripeBuilder.build(),
                new PaymentGatewayCredentials(
                        "bff-payment-gateway-test-token-000000000001",
                        PAYMENT_TOKEN,
                        STRIPE_TOKEN));
    }

    @Test
    void bindsServiceIdentityAndTrustedOwnerToPaymentService() {
        paymentServer.expect(requestTo("https://payment.example.test/api/v1/payments/wallet"))
                .andExpect(header("X-Service-Token", PAYMENT_TOKEN))
                .andExpect(header("X-Payment-Owner", "owner-123"))
                .andExpect(noHeader("X-User-Id"))
                .andRespond(withSuccess(
                        "{\"userId\":\"owner-123\",\"balanceTokens\":20000}",
                        MediaType.APPLICATION_JSON));

        assertEquals("owner-123", service.wallet("owner-123").getUserId());
        paymentServer.verify();
    }

    @Test
    void bindsDistinctStripeIdentityAndMatchingOwnerDuringCheckout() {
        paymentServer.expect(requestTo("https://payment.example.test/api/v1/payments/pricing"))
                .andExpect(header("X-Service-Token", PAYMENT_TOKEN))
                .andExpect(noHeader("X-User-Id"))
                .andRespond(withSuccess(
                        "{\"plans\":[{\"id\":\"starter\",\"name\":\"Starter\","
                                + "\"tokenAmount\":100000,\"priceGbpPence\":499}]}",
                        MediaType.APPLICATION_JSON));
        stripeServer.expect(requestTo("https://stripe.example.test/api/v1/stripe/checkout-sessions"))
                .andExpect(header("X-Service-Token", STRIPE_TOKEN))
                .andExpect(header("X-Payment-Owner", "owner-123"))
                .andExpect(noHeader("X-User-Id"))
                .andExpect(content().json(
                        "{\"userId\":\"owner-123\",\"pricingPlanId\":\"starter\","
                                + "\"tokenAmount\":100000,\"priceGbpPence\":499}"))
                .andRespond(withSuccess(
                        "{\"sessionId\":\"cs_test_123\","
                                + "\"checkoutUrl\":\"https://checkout.example.test/cs_test_123\"}",
                        MediaType.APPLICATION_JSON));

        CheckoutRequest request = new CheckoutRequest();
        request.setPricingPlanId("starter");

        assertEquals("cs_test_123", service.checkout("owner-123", request).getSessionId());
        paymentServer.verify();
        stripeServer.verify();
    }

    private static RequestMatcher noHeader(String name) {
        return request -> assertFalse(request.getHeaders().containsKey(name));
    }
}
