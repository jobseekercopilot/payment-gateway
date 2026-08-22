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
import com.jobseekercopilot.paymentgateway.security.PaymentGatewayIdentityFilter;
import com.jobseekercopilot.paymentgateway.service.PaymentGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Hidden
public class PaymentGatewayController {
    private final PaymentGatewayService paymentGatewayService;

    @GetMapping("/wallet")
    @Operation(
            summary = "Get the current user's AI token wallet",
            parameters = @Parameter(
                    in = ParameterIn.HEADER,
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    required = true))
    @SecurityRequirement(name = "serviceToken")
    public ResponseEntity<WalletSummaryResponse> wallet(
            @RequestAttribute(PaymentGatewayIdentityFilter.OWNER_ATTRIBUTE) String owner) {
        return ResponseEntity.ok(paymentGatewayService.wallet(owner));
    }

    @GetMapping("/transactions")
    @Operation(
            summary = "Get the current user's AI token transactions",
            parameters = @Parameter(
                    in = ParameterIn.HEADER,
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    required = true))
    @SecurityRequirement(name = "serviceToken")
    public ResponseEntity<TransactionsResponse> transactions(
            @RequestAttribute(PaymentGatewayIdentityFilter.OWNER_ATTRIBUTE) String owner,
            @RequestParam(name = "limit", defaultValue = "20") int limit) {
        return ResponseEntity.ok(paymentGatewayService.transactions(owner, limit));
    }

    @GetMapping("/pricing")
    @Operation(
            summary = "Get active AI token pricing plans",
            parameters = @Parameter(
                    in = ParameterIn.HEADER,
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    required = true))
    @SecurityRequirement(name = "serviceToken")
    public ResponseEntity<PricingPlansResponse> pricing(
            @RequestAttribute(PaymentGatewayIdentityFilter.OWNER_ATTRIBUTE) String owner) {
        return ResponseEntity.ok(paymentGatewayService.pricing());
    }

    @PostMapping("/demo-purchase")
    @Operation(
            summary = "Demo purchase AI tokens",
            parameters = @Parameter(
                    in = ParameterIn.HEADER,
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    required = true))
    @SecurityRequirement(name = "serviceToken")
    public ResponseEntity<DemoPurchaseResponse> demoPurchase(
            @RequestAttribute(PaymentGatewayIdentityFilter.OWNER_ATTRIBUTE) String owner,
            @Valid @RequestBody DemoPurchaseRequest request) {
        return ResponseEntity.ok(paymentGatewayService.demoPurchase(owner, request));
    }

    @PostMapping("/checkout")
    @Operation(
            summary = "Create a Stripe checkout session for an AI token bundle",
            parameters = @Parameter(
                    in = ParameterIn.HEADER,
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    required = true))
    @SecurityRequirement(name = "serviceToken")
    public ResponseEntity<CheckoutResponse> checkout(
            @RequestAttribute(PaymentGatewayIdentityFilter.OWNER_ATTRIBUTE) String owner,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(paymentGatewayService.checkout(owner, request));
    }

    @PostMapping("/estimate")
    @Operation(
            summary = "Estimate AI token usage",
            parameters = @Parameter(
                    in = ParameterIn.HEADER,
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    required = true))
    @SecurityRequirement(name = "serviceToken")
    public ResponseEntity<EstimateResponse> estimate(
            @RequestAttribute(PaymentGatewayIdentityFilter.OWNER_ATTRIBUTE) String owner,
            @Valid @RequestBody EstimateRequest request) {
        return ResponseEntity.ok(paymentGatewayService.estimate(owner, request));
    }
}
