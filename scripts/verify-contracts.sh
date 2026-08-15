#!/usr/bin/env bash
set -euo pipefail

contract_dir="${1:-src/main/openapi}"
manifest="$contract_dir/SHA256SUMS"

for required_file in \
    "$contract_dir/payment-service.json" \
    "$contract_dir/payment-service.SOURCE" \
    "$contract_dir/stripe-gateway.json" \
    "$contract_dir/stripe-gateway.SOURCE" \
    "$manifest"; do
    if [[ ! -f "$required_file" || -L "$required_file" ]]; then
        echo "contract policy: required regular file is missing or is a symlink: $required_file" >&2
        exit 1
    fi
done

(
    cd "$contract_dir"
    sha256sum --check --strict SHA256SUMS
)

test "$(wc -l < "$contract_dir/payment-service.SOURCE" | tr -d ' ')" = 4
grep -Fx 'repository=jobseekercopilot/payment-service' "$contract_dir/payment-service.SOURCE" >/dev/null
grep -Fx 'revision=ec6691d7c069118829243d901e42b2d52eb32c88' "$contract_dir/payment-service.SOURCE" >/dev/null
grep -Fx 'path=contracts/openapi.json' "$contract_dir/payment-service.SOURCE" >/dev/null
grep -Fx 'sha256=acf21be9eff02aced215fdfce68d7577dfc79695877d4ee0dfc188fcaa1a779a' "$contract_dir/payment-service.SOURCE" >/dev/null

test "$(wc -l < "$contract_dir/stripe-gateway.SOURCE" | tr -d ' ')" = 4
grep -Fx 'repository=jobseekercopilot/stripe-gateway' "$contract_dir/stripe-gateway.SOURCE" >/dev/null
grep -Fx 'revision=791b262ab8f4846fd47a131fface066d0cffdc5f' "$contract_dir/stripe-gateway.SOURCE" >/dev/null
grep -Fx 'path=contracts/openapi.json' "$contract_dir/stripe-gateway.SOURCE" >/dev/null
grep -Fx 'sha256=2926c5270cdeb04e63ffb7272e352e490eb9e784a7d18b262495d7de319f479c' "$contract_dir/stripe-gateway.SOURCE" >/dev/null

jq -e '
    def serviceAuthenticated($operation):
        $operation.security == [{"serviceToken": []}];
    def trustedOwner($operation):
        ($operation.parameters // []
            | any(.name == "X-Payment-Owner" and .in == "header" and .required == true));
    def noLegacyOwner($operation):
        ($operation.parameters // [] | all(.name != "X-User-Id"));
    (.info.version == "3.2.1") and
    (.components.securitySchemes.serviceToken
        | .type == "apiKey" and .in == "header" and .name == "X-Service-Token") and
    (.paths["/api/v1/payments/wallet"].get.operationId == "wallet") and
    serviceAuthenticated(.paths["/api/v1/payments/wallet"].get) and
    trustedOwner(.paths["/api/v1/payments/wallet"].get) and
    noLegacyOwner(.paths["/api/v1/payments/wallet"].get) and
    (.paths["/api/v1/payments/transactions"].get.operationId == "transactions") and
    serviceAuthenticated(.paths["/api/v1/payments/transactions"].get) and
    trustedOwner(.paths["/api/v1/payments/transactions"].get) and
    noLegacyOwner(.paths["/api/v1/payments/transactions"].get) and
    (.paths["/api/v1/payments/pricing"].get.operationId == "pricing") and
    serviceAuthenticated(.paths["/api/v1/payments/pricing"].get) and
    (.paths["/api/v1/payments/demo-purchase"].post.operationId == "demoPurchase") and
    serviceAuthenticated(.paths["/api/v1/payments/demo-purchase"].post) and
    trustedOwner(.paths["/api/v1/payments/demo-purchase"].post) and
    noLegacyOwner(.paths["/api/v1/payments/demo-purchase"].post) and
    (.paths["/api/v1/payments/estimate"].post.operationId == "estimate") and
    serviceAuthenticated(.paths["/api/v1/payments/estimate"].post) and
    trustedOwner(.paths["/api/v1/payments/estimate"].post) and
    noLegacyOwner(.paths["/api/v1/payments/estimate"].post) and
    (.components.schemas.WalletSummaryResponse.properties
        | has("userId") and has("balanceTokens") and has("lifetimePurchasedTokens") and
          has("lifetimeSpentTokens") and has("lifetimeRefundedTokens") and has("freeTrialGranted")) and
    (.components.schemas.PricingPlansResponse.properties.plans.items["$ref"]
        == "#/components/schemas/TokenPricingPlanResponse") and
    (.components.schemas.TokenPricingPlanResponse.properties
        | has("id") and has("name") and has("description") and has("tokenAmount") and has("priceGbpPence"))
' "$contract_dir/payment-service.json" >/dev/null

jq -e '
    (.info.version == "2.2.0") and
    (.components.securitySchemes.serviceToken
        | .type == "apiKey" and .in == "header" and .name == "X-Service-Token") and
    (.paths["/api/v1/stripe/checkout-sessions"].post.operationId == "createCheckoutSession") and
    (.paths["/api/v1/stripe/checkout-sessions"].post.security
        == [{"serviceToken": []}]) and
    (.paths["/api/v1/stripe/checkout-sessions"].post.parameters
        | any(.name == "X-Payment-Owner" and .in == "header" and .required == true)) and
    (.paths["/api/v1/stripe/checkout-sessions"].post.parameters
        | all(.name != "X-User-Id")) and
    (.paths["/api/v1/stripe/checkout-sessions"].post.requestBody.content["application/json"].schema["$ref"]
        == "#/components/schemas/CreateCheckoutSessionRequest") and
    (.paths["/api/v1/stripe/checkout-sessions"].post.responses["200"].content["*/*"].schema["$ref"]
        == "#/components/schemas/CreateCheckoutSessionResponse") and
    (.components.schemas.CreateCheckoutSessionRequest.properties
        | has("userId") and has("pricingPlanId") and has("tokenAmount") and has("priceGbpPence")) and
    (.components.schemas.CreateCheckoutSessionResponse.properties
        | has("sessionId") and has("checkoutUrl"))
' "$contract_dir/stripe-gateway.json" >/dev/null

echo "contract policy: pinned Payment Service and Stripe Gateway sources are intact and compatible"
