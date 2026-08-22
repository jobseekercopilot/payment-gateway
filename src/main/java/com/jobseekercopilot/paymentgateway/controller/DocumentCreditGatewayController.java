package com.jobseekercopilot.paymentgateway.controller;

import com.jobseekercopilot.paymentgateway.dto.CheckoutReadinessResponse;
import com.jobseekercopilot.paymentgateway.dto.DocumentCreditCatalogResponse;
import com.jobseekercopilot.paymentgateway.dto.DocumentCreditCheckoutRequest;
import com.jobseekercopilot.paymentgateway.dto.DocumentCreditCheckoutResponse;
import com.jobseekercopilot.paymentgateway.dto.DocumentCreditTransactionsResponse;
import com.jobseekercopilot.paymentgateway.dto.DocumentCreditWalletResponse;
import com.jobseekercopilot.paymentgateway.dto.PaymentOrderStatusResponse;
import com.jobseekercopilot.paymentgateway.security.PaymentGatewayIdentityFilter;
import com.jobseekercopilot.paymentgateway.service.DocumentCreditGatewayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.jobseekercopilot.paymentgateway.exception.PaymentGatewayErrorResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "serviceToken")
public class DocumentCreditGatewayController {
    private final DocumentCreditGatewayService service;

    @GetMapping("/catalog")
    @Operation(
            operationId = "getDocumentCreditCatalog",
            parameters = @Parameter(
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    in = ParameterIn.HEADER,
                    required = true))
    public DocumentCreditCatalogResponse catalog(
            @Parameter(
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    in = ParameterIn.HEADER,
                    required = true)
            @RequestAttribute(PaymentGatewayIdentityFilter.OWNER_ATTRIBUTE) String owner) {
        return service.catalog(owner);
    }

    @GetMapping("/wallet")
    @Operation(
            operationId = "getDocumentCreditWallet",
            parameters = @Parameter(
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    in = ParameterIn.HEADER,
                    required = true))
    public DocumentCreditWalletResponse wallet(
            @Parameter(
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    in = ParameterIn.HEADER,
                    required = true)
            @RequestAttribute(PaymentGatewayIdentityFilter.OWNER_ATTRIBUTE) String owner) {
        return service.wallet(owner);
    }

    @GetMapping("/transactions")
    @Operation(
            operationId = "listDocumentCreditTransactions",
            parameters = @Parameter(
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    in = ParameterIn.HEADER,
                    required = true))
    public DocumentCreditTransactionsResponse transactions(
            @Parameter(
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    in = ParameterIn.HEADER,
                    required = true)
            @RequestAttribute(PaymentGatewayIdentityFilter.OWNER_ATTRIBUTE) String owner,
            @RequestParam(defaultValue = "20") int limit) {
        return service.transactions(owner, limit);
    }

    @GetMapping("/checkout-readiness")
    @Operation(
            operationId = "getDocumentCreditCheckoutReadiness",
            parameters = @Parameter(
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    in = ParameterIn.HEADER,
                    required = true))
    public CheckoutReadinessResponse readiness(
            @Parameter(
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    in = ParameterIn.HEADER,
                    required = true)
            @RequestAttribute(PaymentGatewayIdentityFilter.OWNER_ATTRIBUTE) String owner) {
        return service.readiness(owner);
    }

    @PostMapping("/checkout")
    @Operation(
            operationId = "createDocumentCreditCheckout",
            parameters = @Parameter(
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    in = ParameterIn.HEADER,
                    required = true))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Checkout session created"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request or idempotency key",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = PaymentGatewayErrorResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Idempotency key conflict",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = PaymentGatewayErrorResponse.class))),
        @ApiResponse(
                responseCode = "422",
                description = "Consumer acknowledgements are required",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = PaymentGatewayErrorResponse.class))),
        @ApiResponse(
                responseCode = "502",
                description = "Payment downstream failed",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = PaymentGatewayErrorResponse.class))),
        @ApiResponse(
                responseCode = "503",
                description = "Checkout is not ready",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = PaymentGatewayErrorResponse.class)))
    })
    public DocumentCreditCheckoutResponse checkout(
            @Parameter(
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    in = ParameterIn.HEADER,
                    required = true)
            @RequestAttribute(PaymentGatewayIdentityFilter.OWNER_ATTRIBUTE) String owner,
            @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER, required = true)
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody DocumentCreditCheckoutRequest request) {
        return service.checkout(owner, idempotencyKey, request);
    }

    @GetMapping("/orders/{orderId}/status")
    @Operation(
            operationId = "getDocumentCreditOrderStatus",
            parameters = @Parameter(
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    in = ParameterIn.HEADER,
                    required = true))
    public PaymentOrderStatusResponse orderStatus(
            @Parameter(
                    name = PaymentGatewayIdentityFilter.OWNER_HEADER,
                    in = ParameterIn.HEADER,
                    required = true)
            @RequestAttribute(PaymentGatewayIdentityFilter.OWNER_ATTRIBUTE) String owner,
            @PathVariable UUID orderId) {
        return service.orderStatus(owner, orderId);
    }
}
