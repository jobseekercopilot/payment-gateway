# Payment Gateway

Browser-facing Spring Boot gateway for Job Seeker Copilot AI Credit wallet,
pricing, checkout and transaction APIs.

This repository is a sanitised audit baseline, not a beta-ready payment release.
The current source requires locally supplied generated Payment Service and Stripe
Gateway client JARs. Those binaries are intentionally not committed.

See [`docs/BETA_READINESS_AUDIT.md`](docs/BETA_READINESS_AUDIT.md) for verified
security, build and product findings.

## Build

Java 17 and Maven are required.

```bash
mvn -B clean verify
```

The command fails in a clean clone until generated clients are made reproducible.
Do not commit JARs as a workaround.

The OpenAPI contract captured during the source audit is in
`contracts/openapi.json`.

## Licence

Proprietary and confidential. See `LICENSE`.
