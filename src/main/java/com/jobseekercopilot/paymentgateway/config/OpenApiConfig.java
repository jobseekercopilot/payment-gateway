package com.jobseekercopilot.paymentgateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI paymentGatewayOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(
                        "serviceToken",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Service-Token")
                                .description("Dedicated Job Seeker Copilot BFF service identity.")))
                .info(new Info()
                        .title("Payment Gateway API")
                        .description("Frontend-facing gateway for the server-owned document-credit catalog, "
                                + "wallet, transactions, checkout readiness and durable order status. Legacy AI "
                                + "token routes remain available during migration.")
                        .version("2.1.0")
                        .contact(new Contact().name("Jobseeker Copilot"))
                        .license(new License().name("MIT")));
    }

    @Bean
    public OpenApiCustomizer documentCreditContractCustomizer() {
        return openApi -> {
            required(openApi, "CheckoutReadinessResponse",
                    "checkoutAvailable", "code", "paymentServiceCode", "providerCode", "mode");
            required(openApi, "DocumentCreditCatalogResponse",
                    "catalogVersion", "currency", "billingCountry", "taxTreatment", "taxStatus",
                    "displayedPriceIsCheckoutTotal", "automaticRenewal", "creditUnit",
                    "freeAllowanceCredits", "plans", "promotion");
            required(openApi, "Plan",
                    "id", "name", "description", "documentCredits", "priceMinor", "currency",
                    "fullApplicationEquivalent", "promotionBonusDocumentCredits", "active",
                    "sortOrder");
            required(openApi, "Promotion",
                    "id", "enabled", "status", "bonusPercent", "customerLimit");
            required(openApi, "DocumentCreditCheckoutResponse",
                    "orderId", "checkoutSessionId", "url", "status", "expiresAt",
                    "pricingSnapshot", "promotionBonusDocumentCredits", "promotionGuaranteed",
                    "consumerTermsVersion", "consumerAcknowledgementsRecorded");
            required(openApi, "PricingSnapshot",
                    "catalogVersion", "pricingPlanId", "pricingPlanName", "documentCredits",
                    "priceMinor", "currency", "billingCountry", "taxTreatment", "taxStatus",
                    "legalEntityType", "legalEntityConfigurationVersion",
                    "displayedPriceIsCheckoutTotal");
            required(openApi, "DocumentCreditWalletResponse",
                    "balanceDocumentCredits", "lifetimePurchasedDocumentCredits",
                    "lifetimeSpentDocumentCredits", "lifetimeReversedDocumentCredits",
                    "reviewDebtDocumentCredits", "freeAllowanceGranted", "status");
            required(openApi, "DocumentCreditTransactionsResponse", "transactions");
            required(openApi, "Transaction",
                    "id", "type", "documentCredits", "balanceBeforeDocumentCredits",
                    "balanceAfterDocumentCredits", "operationId", "description", "createdAt");
            required(openApi, "PaymentOrderStatusResponse",
                    "orderId", "status", "pricingPlanId", "documentCredits",
                    "promotionBonusDocumentCredits", "totalGrantedDocumentCredits", "priceMinor",
                    "currency", "taxTreatment", "taxStatus", "legalEntityType",
                    "legalEntityConfigurationVersion", "createdAt", "expiresAt", "creditsAdded",
                    "messageCode");
            required(openApi, "PaymentGatewayErrorResponse", "error", "code", "message");

            enumerated(openApi, "DocumentCreditCatalogResponse", "currency", "GBP");
            enumerated(openApi, "DocumentCreditCatalogResponse", "billingCountry", "GB");
            enumerated(openApi, "DocumentCreditCatalogResponse", "taxTreatment",
                    "VAT_NOT_CHARGED", "VAT_INCLUDED");
            enumerated(openApi, "DocumentCreditCatalogResponse", "taxStatus",
                    "NOT_CONFIGURED", "NOT_VAT_REGISTERED", "VAT_REGISTERED");
            enumerated(openApi, "DocumentCreditCatalogResponse", "creditUnit", "DOCUMENT");
            enumerated(openApi, "Plan", "currency", "GBP");
            enumerated(openApi, "PricingSnapshot", "currency", "GBP");
            enumerated(openApi, "PricingSnapshot", "billingCountry", "GB");
            enumerated(openApi, "PricingSnapshot", "taxTreatment",
                    "VAT_NOT_CHARGED", "VAT_INCLUDED");
            enumerated(openApi, "PricingSnapshot", "taxStatus",
                    "NOT_CONFIGURED", "NOT_VAT_REGISTERED", "VAT_REGISTERED");
            enumerated(openApi, "PricingSnapshot", "legalEntityType",
                    "NOT_CONFIGURED", "SOLE_TRADER", "LIMITED_COMPANY");
            enumerated(openApi, "DocumentCreditWalletResponse", "status",
                    "ACTIVE", "BLOCKED_REVIEW", "REVOKED");
            enumerated(openApi, "PaymentOrderStatusResponse", "currency", "GBP");
            enumerated(openApi, "PaymentOrderStatusResponse", "taxTreatment",
                    "VAT_NOT_CHARGED", "VAT_INCLUDED");
            enumerated(openApi, "PaymentOrderStatusResponse", "taxStatus",
                    "NOT_CONFIGURED", "NOT_VAT_REGISTERED", "VAT_REGISTERED");
            enumerated(openApi, "PaymentOrderStatusResponse", "legalEntityType",
                    "NOT_CONFIGURED", "SOLE_TRADER", "LIMITED_COMPANY");
            enumerated(openApi, "CheckoutReadinessResponse", "code",
                    "READY", "PAYMENTS_DISABLED", "LIVE_RELEASE_NOT_AUTHORISED",
                    "TAX_STATUS_NOT_CONFIGURED", "LEGAL_ENTITY_NOT_CONFIGURED",
                    "PROVIDER_UNAVAILABLE", "PAYMENT_SERVICE_UNAVAILABLE",
                    "PAYMENT_PROVIDER_UNAVAILABLE");
            enumerated(openApi, "CheckoutReadinessResponse", "paymentServiceCode",
                    "READY", "PAYMENTS_DISABLED", "LIVE_RELEASE_NOT_AUTHORISED",
                    "TAX_STATUS_NOT_CONFIGURED", "LEGAL_ENTITY_NOT_CONFIGURED",
                    "PROVIDER_UNAVAILABLE", "UNAVAILABLE");
            enumerated(openApi, "CheckoutReadinessResponse", "providerCode",
                    "READY", "PAYMENTS_DISABLED", "LIVE_RELEASE_NOT_AUTHORISED",
                    "NOT_CHECKED", "UNAVAILABLE");
            enumerated(openApi, "CheckoutReadinessResponse", "mode",
                    "TEST", "LIVE", "FIXTURE", "DISABLED", "UNAVAILABLE");
            nullable(openApi, "Transaction", "referenceType", "referenceId");
            nullable(openApi, "PaymentOrderStatusResponse", "fulfilledAt");
        };
    }

    private static void required(OpenAPI openApi, String schemaName, String... fields) {
        schema(openApi, schemaName).setRequired(List.of(fields));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void enumerated(
            OpenAPI openApi, String schemaName, String propertyName, Object... values) {
        Schema property = (Schema) schema(openApi, schemaName).getProperties().get(propertyName);
        property.setEnum(List.of(values));
    }

    private static void nullable(OpenAPI openApi, String schemaName, String... propertyNames) {
        for (String propertyName : propertyNames) {
            Schema<?> property = (Schema<?>) schema(openApi, schemaName)
                    .getProperties().get(propertyName);
            property.setNullable(true);
        }
    }

    private static Schema<?> schema(OpenAPI openApi, String schemaName) {
        Schema<?> schema = openApi.getComponents().getSchemas().get(schemaName);
        if (schema == null) {
            throw new IllegalStateException("OpenAPI schema is missing: " + schemaName);
        }
        return schema;
    }
}
