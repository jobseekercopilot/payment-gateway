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
    (.info.version == "2.0.0") and
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
          has("lifetimeSpentTokens") and has("lifetimeRefundedTokens") and has("freeTrialGranted"))
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
