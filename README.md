# Payment Gateway

## Role in Job Seeker Copilot

| Role | Called by | Calls | Data | Local port |
|---|---|---|---|---:|
| AI-credit wallet/pricing/checkout facade | Future trusted BFF path | Payment Service, Stripe Gateway | None | 8098 |

The backend is composed, but the client BFF returns `FEATURE_NOT_AVAILABLE` for `/api/v1/payment/**` on `develop`. See the central [payment status](https://docs.jobseekercopilot.com/journeys/reporting-payments/) and [implementation status](https://docs.jobseekercopilot.com/reference/implementation-status/).

Browser-facing Spring Boot gateway for Job Seeker Copilot AI Credit wallet,
pricing, checkout and transaction APIs.

This repository is a sanitised audit baseline, not a beta-ready payment release.
The gateway builds from committed source without copied generated-client JARs.
Its handwritten downstream adapters are checked against reviewed, checksum-
protected Payment Service and Stripe Gateway producer contracts.

See [`docs/BETA_READINESS_AUDIT.md`](docs/BETA_READINESS_AUDIT.md) for verified
security, build and product findings.
See [`docs/PAYMENT_IDENTITY_BOUNDARY.md`](docs/PAYMENT_IDENTITY_BOUNDARY.md)
for the BFF and downstream service trust boundary.

## Build

Java 17 and Maven are required.

```bash
mvn -B clean verify
```

The build needs no sibling checkout, `libs` directory or generated binary.

The OpenAPI contract captured during the source audit is in
`contracts/openapi.json`.

The gateway requires `BFF_TO_PAYMENT_GATEWAY_TOKEN` on ingress and uses
`PAYMENT_GATEWAY_TO_PAYMENT_SERVICE_TOKEN` and
`PAYMENT_GATEWAY_TO_STRIPE_GATEWAY_TOKEN` on the two downstream boundaries.
Each token must contain at least 32 bytes and all three must be distinct.

Run the pinned downstream compatibility gates with:

```bash
./scripts/test-contract-policy.sh
./scripts/verify-contracts.sh
./scripts/test-api-contract-policy.sh
./scripts/verify-api-contract.sh
```

See [`docs/CONTRACT_GOVERNANCE.md`](docs/CONTRACT_GOVERNANCE.md) for producer
ownership, revision/checksum pins, regeneration and rollback.

## Licence

Proprietary and confidential. See `LICENSE`.
