package com.jobseekercopilot.paymentgateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobseekercopilot.paymentgateway.dto.CheckoutRequest;
import com.jobseekercopilot.paymentgateway.dto.CheckoutResponse;
import com.jobseekercopilot.paymentgateway.dto.DemoPurchaseRequest;
import com.jobseekercopilot.paymentgateway.dto.DemoPurchaseResponse;
import com.jobseekercopilot.paymentgateway.dto.PricingPlansResponse;
import com.jobseekercopilot.paymentgateway.dto.TokenPricingPlanResponse;
import com.jobseekercopilot.paymentgateway.dto.WalletSummaryResponse;
import com.jobseekercopilot.paymentgateway.service.PaymentGatewayService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class PaymentGatewayControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private PaymentGatewayService paymentGatewayService;

    @Test
    void walletEndpointCallsPaymentService() throws Exception {
        WalletSummaryResponse response = new WalletSummaryResponse();
        response.setUserId("user-123");
        response.setBalanceTokens(20000);
        response.setFreeTrialGranted(true);
        when(paymentGatewayService.wallet(eq("user-123"))).thenReturn(response);

        mockMvc.perform(get("/api/v1/payment/wallet").header("X-User-Id", "user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.balanceTokens").value(20000));
    }

    @Test
    void pricingEndpointCallsPaymentService() throws Exception {
        TokenPricingPlanResponse plan = new TokenPricingPlanResponse();
        plan.setId("starter");
        plan.setName("Starter");
        plan.setTokenAmount(100000);
        plan.setPriceGbpPence(799);
        PricingPlansResponse response = new PricingPlansResponse();
        response.setPlans(List.of(plan));
        when(paymentGatewayService.pricing()).thenReturn(response);

        mockMvc.perform(get("/api/v1/payment/pricing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plans[0].id").value("starter"))
                .andExpect(jsonPath("$.plans[0].tokenAmount").value(100000));
    }

    @Test
    void demoPurchaseEndpointCallsPaymentService() throws Exception {
        WalletSummaryResponse wallet = new WalletSummaryResponse();
        wallet.setUserId("user-123");
        wallet.setBalanceTokens(120000);
        DemoPurchaseResponse response = new DemoPurchaseResponse();
        response.setWallet(wallet);
        when(paymentGatewayService.demoPurchase(eq("user-123"), any(DemoPurchaseRequest.class))).thenReturn(response);

        DemoPurchaseRequest request = new DemoPurchaseRequest();
        request.setPricingPlanId("starter");
        mockMvc.perform(post("/api/v1/payment/demo-purchase")
                        .header("X-User-Id", "user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wallet.balanceTokens").value(120000));
    }

    @Test
    void checkoutEndpointReturnsStripeCheckoutUrl() throws Exception {
        CheckoutResponse response = new CheckoutResponse();
        response.setSessionId("cs_test_123");
        response.setCheckoutUrl("https://checkout.stripe.com/c/pay/cs_test_123");
        when(paymentGatewayService.checkout(eq("user-123"), any(CheckoutRequest.class))).thenReturn(response);

        CheckoutRequest request = new CheckoutRequest();
        request.setPricingPlanId("starter");
        mockMvc.perform(post("/api/v1/payment/checkout")
                        .header("X-User-Id", "user-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("cs_test_123"))
                .andExpect(jsonPath("$.checkoutUrl").value("https://checkout.stripe.com/c/pay/cs_test_123"));
    }

    @Test
    void checkoutRequiresUserId() throws Exception {
        CheckoutRequest request = new CheckoutRequest();
        request.setPricingPlanId("starter");

        mockMvc.perform(post("/api/v1/payment/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing X-User-Id header"));
    }

    @Test
    void missingUserIdReturnsControlledError() throws Exception {
        mockMvc.perform(get("/api/v1/payment/wallet"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing X-User-Id header"));
    }
}
