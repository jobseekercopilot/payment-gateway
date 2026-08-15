#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
temporary_dir="$(mktemp -d)"
trap 'rm -rf "$temporary_dir"' EXIT

copy_contracts() {
    local destination="$1"
    mkdir -p "$destination"
    cp "$repository_root"/src/main/openapi/* "$destination/"
}

"$repository_root/scripts/verify-contracts.sh" "$repository_root/src/main/openapi" >/dev/null

copy_contracts "$temporary_dir/checksum-drift"
jq '.info.description = "unreviewed drift"' \
    "$temporary_dir/checksum-drift/payment-service.json" \
    > "$temporary_dir/checksum-drift/changed.json"
mv "$temporary_dir/checksum-drift/changed.json" "$temporary_dir/checksum-drift/payment-service.json"
if "$repository_root/scripts/verify-contracts.sh" "$temporary_dir/checksum-drift" >/dev/null 2>&1; then
    echo "contract policy negative test accepted checksum drift" >&2
    exit 1
fi

copy_contracts "$temporary_dir/payment-operation"
jq 'del(.paths["/api/v1/payments/wallet"])' \
    "$temporary_dir/payment-operation/payment-service.json" \
    > "$temporary_dir/payment-operation/changed.json"
mv "$temporary_dir/payment-operation/changed.json" "$temporary_dir/payment-operation/payment-service.json"
(cd "$temporary_dir/payment-operation" && sha256sum payment-service.json stripe-gateway.json > SHA256SUMS)
if "$repository_root/scripts/verify-contracts.sh" "$temporary_dir/payment-operation" >/dev/null 2>&1; then
    echo "contract policy negative test accepted removal of the wallet operation" >&2
    exit 1
fi

copy_contracts "$temporary_dir/stripe-field"
jq 'del(.components.schemas.CreateCheckoutSessionResponse.properties.checkoutUrl)' \
    "$temporary_dir/stripe-field/stripe-gateway.json" \
    > "$temporary_dir/stripe-field/changed.json"
mv "$temporary_dir/stripe-field/changed.json" "$temporary_dir/stripe-field/stripe-gateway.json"
(cd "$temporary_dir/stripe-field" && sha256sum payment-service.json stripe-gateway.json > SHA256SUMS)
if "$repository_root/scripts/verify-contracts.sh" "$temporary_dir/stripe-field" >/dev/null 2>&1; then
    echo "contract policy negative test accepted removal of checkoutUrl" >&2
    exit 1
fi

copy_contracts "$temporary_dir/source-revision"
sed 's/^revision=.*/revision=0000000000000000000000000000000000000000/' \
    "$temporary_dir/source-revision/payment-service.SOURCE" \
    > "$temporary_dir/source-revision/changed.SOURCE"
mv "$temporary_dir/source-revision/changed.SOURCE" "$temporary_dir/source-revision/payment-service.SOURCE"
if "$repository_root/scripts/verify-contracts.sh" "$temporary_dir/source-revision" >/dev/null 2>&1; then
    echo "contract policy negative test accepted unreviewed producer revision metadata" >&2
    exit 1
fi

copy_contracts "$temporary_dir/payment-identity"
jq 'del(.paths["/api/v1/payments/wallet"].get.security)' \
    "$temporary_dir/payment-identity/payment-service.json" \
    > "$temporary_dir/payment-identity/changed.json"
mv "$temporary_dir/payment-identity/changed.json" "$temporary_dir/payment-identity/payment-service.json"
(cd "$temporary_dir/payment-identity" && sha256sum payment-service.json stripe-gateway.json > SHA256SUMS)
if "$repository_root/scripts/verify-contracts.sh" "$temporary_dir/payment-identity" >/dev/null 2>&1; then
    echo "contract policy negative test accepted removal of Payment Service authentication" >&2
    exit 1
fi

copy_contracts "$temporary_dir/stripe-owner"
jq '.paths["/api/v1/stripe/checkout-sessions"].post.parameters
        |= map(select(.name != "X-Payment-Owner"))' \
    "$temporary_dir/stripe-owner/stripe-gateway.json" \
    > "$temporary_dir/stripe-owner/changed.json"
mv "$temporary_dir/stripe-owner/changed.json" "$temporary_dir/stripe-owner/stripe-gateway.json"
(cd "$temporary_dir/stripe-owner" && sha256sum payment-service.json stripe-gateway.json > SHA256SUMS)
if "$repository_root/scripts/verify-contracts.sh" "$temporary_dir/stripe-owner" >/dev/null 2>&1; then
    echo "contract policy negative test accepted removal of Stripe checkout ownership" >&2
    exit 1
fi

echo "contract policy negative tests passed"
