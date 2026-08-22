#!/usr/bin/env bash
set -euo pipefail

contract="${1:-contracts/openapi.json}"
generated="${2:-}"
contract_dir="$(dirname "$contract")"
manifest="$contract_dir/SHA256SUMS"

for required_file in "$contract" "$manifest"; do
    if [[ ! -f "$required_file" || -L "$required_file" ]]; then
        echo "API contract policy: required regular file is missing or is a symlink: $required_file" >&2
        exit 1
    fi
done

(
    cd "$contract_dir"
    sha256sum --check --strict SHA256SUMS
)

jq -e '
    def serviceAuthenticated($operation):
        $operation.security == [{"serviceToken": []}];
    def trustedOwner($operation):
        ($operation.parameters // []
            | any(.name == "X-Payment-Owner" and .in == "header" and .required == true));
    def noLegacyOwner($operation):
        ($operation.parameters // [] | all(.name != "X-User-Id"));
    (.openapi | type == "string" and startswith("3.")) and
    (.info.version == "2.2.0") and
    (.components.securitySchemes.serviceToken
        | .type == "apiKey" and .in == "header" and .name == "X-Service-Token") and
    (.paths["/api/v1/payment/wallet"].get.operationId == "wallet") and
    (.paths["/api/v1/payment/transactions"].get.operationId == "transactions") and
    (.paths["/api/v1/payment/pricing"].get.operationId == "pricing") and
    (.paths["/api/v1/payment/demo-purchase"].post.operationId == "demoPurchase") and
    (.paths["/api/v1/payment/checkout"].post.operationId == "checkout") and
    (.paths["/api/v1/payment/estimate"].post.operationId == "estimate") and
    ([.paths[] | .[]]
        | all(serviceAuthenticated(.) and trustedOwner(.) and noLegacyOwner(.))) and
    (.components.schemas.CheckoutRequest.required == ["pricingPlanId"]) and
    (.components.schemas.CheckoutResponse.properties | has("sessionId") and has("checkoutUrl")) and
    (.components.schemas.WalletSummaryResponse.properties
        | has("userId") and has("balanceTokens") and has("lifetimePurchasedTokens") and
          has("lifetimeSpentTokens") and has("lifetimeRefundedTokens") and has("freeTrialGranted")) and
    (.paths["/api/v2/payments/catalog"].get.operationId == "getDocumentCreditCatalog") and
    (.paths["/api/v2/payments/checkout-readiness"].get.operationId
        == "getDocumentCreditCheckoutReadiness") and
    (.paths["/api/v2/payments/checkout"].post.operationId
        == "createDocumentCreditCheckout") and
    (.paths["/api/v2/payments/orders/{orderId}/status"].get.operationId
        == "getDocumentCreditOrderStatus") and
    (.paths["/api/v2/payments/checkout"].post.parameters
        | any(.name == "Idempotency-Key" and .in == "header" and .required == true)) and
    (.components.schemas.DocumentCreditCheckoutRequest.required
        | index("pricingPlanId") != null and index("billingCountry") != null and
          index("immediateSupplyRequested") != null and
          index("cancellationRightLossAcknowledged") != null) and
    (.components.schemas.DocumentCreditCheckoutRequest.properties.immediateSupplyRequested.enum
        == [true]) and
    (.components.schemas.DocumentCreditCheckoutResponse.properties
        | has("orderId") and has("url") and has("promotionGuaranteed") and
          has("consumerAcknowledgementsRecorded") and has("pricingSnapshot")) and
    (.components.schemas.DocumentCreditCheckoutResponse.required | length == 10) and
    (.components.schemas.PricingSnapshot.properties
        | has("taxTreatment") and has("taxStatus") and has("legalEntityType") and
          has("legalEntityConfigurationVersion") and has("displayedPriceIsCheckoutTotal")) and
    (.components.schemas.PricingSnapshot.required | length == 12) and
    (.components.schemas.PricingSnapshot.properties.taxTreatment.enum
        == ["VAT_NOT_CHARGED", "VAT_INCLUDED"]) and
    (.components.schemas.PricingSnapshot.properties.taxStatus.enum
        == ["NOT_CONFIGURED", "NOT_VAT_REGISTERED", "VAT_REGISTERED"]) and
    (.components.schemas.PricingSnapshot.properties.legalEntityType.enum
        == ["NOT_CONFIGURED", "SOLE_TRADER", "LIMITED_COMPANY"]) and
    (.components.schemas.DocumentCreditCatalogResponse.required | length == 11) and
    (.components.schemas.DocumentCreditWalletResponse.required | length == 7) and
    (.components.schemas.DocumentCreditTransactionsResponse.required == ["transactions"]) and
    (.components.schemas.PaymentOrderStatusResponse.required | length == 16) and
    (.components.schemas.CheckoutReadinessResponse.required | length == 5) and
    (.components.schemas.PaymentGatewayErrorResponse.required
        | sort == ["code", "error", "message"]) and
    (.paths["/api/v2/payments/checkout"].post.responses["422"].content["application/json"].schema["$ref"]
        == "#/components/schemas/PaymentGatewayErrorResponse")
' "$contract" >/dev/null

if [[ -n "$generated" ]]; then
    if [[ ! -f "$generated" || -L "$generated" ]]; then
        echo "API contract policy: generated contract is missing or is a symlink: $generated" >&2
        exit 1
    fi
    temporary_dir="$(mktemp -d)"
    trap 'rm -rf "$temporary_dir"' EXIT
    jq -S . "$contract" > "$temporary_dir/reviewed.json"
    jq -S . "$generated" > "$temporary_dir/generated.json"
    if ! cmp -s "$temporary_dir/reviewed.json" "$temporary_dir/generated.json"; then
        echo "API contract policy: generated OpenAPI differs from contracts/openapi.json" >&2
        diff -u "$temporary_dir/reviewed.json" "$temporary_dir/generated.json" >&2 || true
        exit 1
    fi
fi

echo "API contract policy: reviewed Payment Gateway contract is intact and compatible"
