# Payment Gateway contract governance

Payment Service owns the wallet, pricing and transaction API. Stripe Gateway
owns the provider-adapter API. Payment Gateway vendors their reviewed OpenAPI
bytes only as compatibility inputs for its handwritten adapters; it does not
become the producer or silently edit either contract.

`src/main/openapi/*.SOURCE` records the exact private producer repository,
merged revision, source path and SHA-256. `SHA256SUMS` protects the reviewed
bytes. `scripts/verify-contracts.sh` checks every operation and field used by
`PaymentGatewayService`, while `scripts/test-contract-policy.sh` proves that
checksum, revision and breaking-shape drift fail closed.

The removed `systemPath` dependencies were unused opaque binaries. They are not
replaced with copied JARs because the gateway's current adapters do not import
generated client types.

Payment Gateway owns `contracts/openapi.json` for its browser-facing API.
`contracts/SHA256SUMS`, the API policy and post-build equality check against
`target/openapi.json` keep that producer source authoritative.

## Updating a pin

1. Merge and verify the producer contract change.
2. Review compatibility and security/ownership impact.
3. Copy the exact producer-owned contract bytes.
4. Update the matching `.SOURCE` revision/path/checksum and `SHA256SUMS`.
5. Update policy assertions only for an intentional reviewed consumer change.
6. Run the policy scripts, `mvn -B clean verify` and the source-only container
   build.

Rollback restores the prior contract bytes, metadata and consumer code
together. Generated binaries and sibling checkouts are never a build input.
