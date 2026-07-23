package com.jobseekercopilot.paymentgateway.controller;

import com.jobseekercopilot.paymentgateway.dto.CheckoutRequest;
import com.jobseekercopilot.paymentgateway.dto.CheckoutResponse;
import com.jobseekercopilot.paymentgateway.dto.DemoPurchaseRequest;
import com.jobseekercopilot.paymentgateway.dto.DemoPurchaseResponse;
import com.jobseekercopilot.paymentgateway.dto.EstimateRequest;
import com.jobseekercopilot.paymentgateway.dto.EstimateResponse;
import com.jobseekercopilot.paymentgateway.dto.PricingPlansResponse;
import com.jobseekercopilot.paymentgateway.dto.TransactionsResponse;
import com.jobseekercopilot.paymentgateway.dto.WalletSummaryResponse;
import com.jobseekercopilot.paymentgateway.exception.MissingUserIdException;
import com.jobseekercopilot.paymentgateway.service.PaymentGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentGatewayController {
    private static final String USER_ID_HEADER = "X-User-Id";
    private final PaymentGatewayService paymentGatewayService;

    @GetMapping("/wallet")
    @Operation(summary = "Get the current user's AI token wallet")
    public ResponseEntity<WalletSummaryResponse> wallet(
            @Parameter(in = ParameterIn.HEADER, name = USER_ID_HEADER, required = true)
            @RequestHeader(name = USER_ID_HEADER, required = false) String userId) {
        return ResponseEntity.ok(paymentGatewayService.wallet(requireUserId(userId)));
    }

    @GetMapping("/transactions")
    @Operation(summary = "Get the current user's AI token transactions")
    public ResponseEntity<TransactionsResponse> transactions(
            @Parameter(in = ParameterIn.HEADER, name = USER_ID_HEADER, required = true)
            @RequestHeader(name = USER_ID_HEADER, required = false) String userId,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        return ResponseEntity.ok(paymentGatewayService.transactions(requireUserId(userId), limit));
    }

    @GetMapping("/pricing")
    @Operation(summary = "Get active AI token pricing plans")
    public ResponseEntity<PricingPlansResponse> pricing() {
        return ResponseEntity.ok(paymentGatewayService.pricing());
    }

    @PostMapping("/demo-purchase")
    @Operation(summary = "Demo purchase AI tokens")
    public ResponseEntity<DemoPurchaseResponse> demoPurchase(
            @Parameter(in = ParameterIn.HEADER, name = USER_ID_HEADER, required = true)
            @RequestHeader(name = USER_ID_HEADER, required = false) String userId,
            @Valid @RequestBody DemoPurchaseRequest request) {
        return ResponseEntity.ok(paymentGatewayService.demoPurchase(requireUserId(userId), request));
    }

    @PostMapping("/checkout")
    @Operation(summary = "Create a Stripe checkout session for an AI token bundle")
    public ResponseEntity<CheckoutResponse> checkout(
            @Parameter(in = ParameterIn.HEADER, name = USER_ID_HEADER, required = true)
            @RequestHeader(name = USER_ID_HEADER, required = false) String userId,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(paymentGatewayService.checkout(requireUserId(userId), request));
    }

    @PostMapping("/estimate")
    @Operation(summary = "Estimate AI token usage")
    public ResponseEntity<EstimateResponse> estimate(
            @Parameter(in = ParameterIn.HEADER, name = USER_ID_HEADER, required = true)
            @RequestHeader(name = USER_ID_HEADER, required = false) String userId,
            @Valid @RequestBody EstimateRequest request) {
        return ResponseEntity.ok(paymentGatewayService.estimate(requireUserId(userId), request));
    }

    private String requireUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new MissingUserIdException();
        }
        return userId;
    }
}
