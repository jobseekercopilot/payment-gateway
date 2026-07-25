#!/usr/bin/env bash
set -euo pipefail

repository_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
temporary_dir="$(mktemp -d)"
trap 'rm -rf "$temporary_dir"' EXIT

copy_contract() {
    local destination="$1"
    mkdir -p "$destination"
    cp "$repository_root/contracts/openapi.json" "$repository_root/contracts/SHA256SUMS" "$destination/"
}

"$repository_root/scripts/verify-api-contract.sh" "$repository_root/contracts/openapi.json" >/dev/null

copy_contract "$temporary_dir/checksum-drift"
jq '.info.description = "unreviewed drift"' \
    "$temporary_dir/checksum-drift/openapi.json" \
    > "$temporary_dir/checksum-drift/changed.json"
mv "$temporary_dir/checksum-drift/changed.json" "$temporary_dir/checksum-drift/openapi.json"
if "$repository_root/scripts/verify-api-contract.sh" "$temporary_dir/checksum-drift/openapi.json" >/dev/null 2>&1; then
    echo "API contract negative test accepted checksum drift" >&2
    exit 1
fi

copy_contract "$temporary_dir/checkout"
jq 'del(.paths["/api/v1/payment/checkout"].post)' \
    "$temporary_dir/checkout/openapi.json" > "$temporary_dir/checkout/changed.json"
mv "$temporary_dir/checkout/changed.json" "$temporary_dir/checkout/openapi.json"
(cd "$temporary_dir/checkout" && sha256sum openapi.json > SHA256SUMS)
if "$repository_root/scripts/verify-api-contract.sh" "$temporary_dir/checkout/openapi.json" >/dev/null 2>&1; then
    echo "API contract negative test accepted removal of checkout" >&2
    exit 1
fi

copy_contract "$temporary_dir/checkout-url"
jq 'del(.components.schemas.CheckoutResponse.properties.checkoutUrl)' \
    "$temporary_dir/checkout-url/openapi.json" > "$temporary_dir/checkout-url/changed.json"
mv "$temporary_dir/checkout-url/changed.json" "$temporary_dir/checkout-url/openapi.json"
(cd "$temporary_dir/checkout-url" && sha256sum openapi.json > SHA256SUMS)
if "$repository_root/scripts/verify-api-contract.sh" "$temporary_dir/checkout-url/openapi.json" >/dev/null 2>&1; then
    echo "API contract negative test accepted removal of checkoutUrl" >&2
    exit 1
fi

echo "API contract policy negative tests passed"
