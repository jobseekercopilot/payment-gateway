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
    (.paths | keys | length == 6) and
    (.paths | keys | all(startswith("/api/v2/payments/"))) and
    ([.paths[] | .[]]
        | all(serviceAuthenticated(.) and trustedOwner(.) and noLegacyOwner(.))) and
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
    (.components.schemas.DocumentCreditCatalogResponse.properties
        | has("generationUnit") and has("freeAllowanceGenerations") and
          (has("creditUnit") | not) and (has("freeAllowanceCredits") | not)) and
    (.components.schemas.Plan.properties
        | has("documentGenerations") and has("promotionBonusDocumentGenerations") and
          (has("documentCredits") | not) and (has("promotionBonusDocumentCredits") | not)) and
    (.components.schemas.DocumentCreditWalletResponse.required | length == 7) and
    (.components.schemas.DocumentCreditWalletResponse.properties
        | has("remainingDocumentGenerations") and has("lifetimeUsedDocumentGenerations") and
          (has("balanceDocumentCredits") | not) and (has("lifetimeSpentDocumentCredits") | not)) and
    (.components.schemas.DocumentCreditTransactionsResponse.required == ["transactions"]) and
    (.components.schemas.PaymentOrderStatusResponse.required | length == 16) and
    (.components.schemas.CheckoutReadinessResponse.required | length == 5) and
    (.components.schemas.PaymentGatewayErrorResponse.required
        | sort == ["code", "error", "message"]) and
    (.paths["/api/v2/payments/checkout"].post.responses["422"].content["application/json"].schema["$ref"]
        == "#/components/schemas/PaymentGatewayErrorResponse") and
    ((tostring | test("AI token|balanceTokens|tokenAmount|documentCredits|CREDITS_ADDED"; "i")) | not)
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
