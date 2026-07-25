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
grep -Fx 'revision=3175e5730cd0743e15455a0acc8e2bc35b56a78f' "$contract_dir/payment-service.SOURCE" >/dev/null
grep -Fx 'path=contracts/openapi.json' "$contract_dir/payment-service.SOURCE" >/dev/null
grep -Fx 'sha256=2b1bfef95e1ba4c1f191627dfc4972b3ed7a931dead8fbb7aecbf5b793acae7a' "$contract_dir/payment-service.SOURCE" >/dev/null

test "$(wc -l < "$contract_dir/stripe-gateway.SOURCE" | tr -d ' ')" = 4
grep -Fx 'repository=jobseekercopilot/stripe-gateway' "$contract_dir/stripe-gateway.SOURCE" >/dev/null
grep -Fx 'revision=18cc71ddda37152c4e8154b23745217fb895064d' "$contract_dir/stripe-gateway.SOURCE" >/dev/null
grep -Fx 'path=contracts/openapi.json' "$contract_dir/stripe-gateway.SOURCE" >/dev/null
grep -Fx 'sha256=4a10b1da2c525b17c80f995055f358c63c7252ae7f4152109d607aa7dd4bc09c' "$contract_dir/stripe-gateway.SOURCE" >/dev/null

jq -e '
    (.info.version == "1.0.0") and
    (.paths["/api/v1/payments/wallet"].get.operationId == "wallet") and
    (.paths["/api/v1/payments/transactions"].get.operationId == "transactions") and
    (.paths["/api/v1/payments/pricing"].get.operationId == "pricing") and
    (.paths["/api/v1/payments/demo-purchase"].post.operationId == "demoPurchase") and
    (.paths["/api/v1/payments/estimate"].post.operationId == "estimate") and
    (.components.schemas.WalletSummaryResponse.properties
        | has("userId") and has("balanceTokens") and has("lifetimePurchasedTokens") and
          has("lifetimeSpentTokens") and has("lifetimeRefundedTokens") and has("freeTrialGranted")) and
    (.components.schemas.PricingPlansResponse.properties.plans.items["$ref"]
        == "#/components/schemas/TokenPricingPlanResponse") and
    (.components.schemas.TokenPricingPlanResponse.properties
        | has("id") and has("name") and has("description") and has("tokenAmount") and has("priceGbpPence"))
' "$contract_dir/payment-service.json" >/dev/null

jq -e '
    (.info.version == "1.0.0") and
    (.paths["/api/v1/stripe/checkout-sessions"].post.operationId == "createCheckoutSession") and
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
