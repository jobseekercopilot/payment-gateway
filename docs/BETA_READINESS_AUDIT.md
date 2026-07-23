# Payment Gateway beta-readiness audit

Audit date: 2026-07-23  
Decision: **Not ready for private beta**

This is an audit baseline only. No payment behavior was implemented or enabled.

## Verified behavior

- Exposes wallet, transaction, pricing, demo-purchase, checkout and estimate
  operations under `/api/v1/payment`.
- Obtains the selected plan's token and GBP-pence values from Payment Service
  before requesting a Stripe checkout session.
- Propagates correlation IDs and exposes basic Spring Boot health.
- With two inherited local generated-client JARs present, `mvn -q clean verify`
  passed 7 tests with no failures/errors/skips.

## Build result

The POM uses `systemPath` for Payment Service and Stripe Gateway client JARs and
the Dockerfile copies `libs`. Generated binaries are excluded from this private
baseline, so a clean clone and container build are not reproducible.

Unblock condition: publish or deterministically generate versioned clients, remove
`systemPath`/`libs`, and prove clean-clone CI and contract compatibility.

## Critical findings

### Browser-controlled identity

The gateway only requires a nonblank `X-User-Id`; it does not validate an access
token or derive the user from a trusted security context. The retained legacy BFF
also forwards browser identity headers. The current private client correctly places
`/api/v1/payment` behind its beta-disabled fail-closed boundary; it must remain
blocked until the Payments epic is approved and complete.

### Demo balance mutation

`demo-purchase` is a browser-facing balance-changing operation. No environment or
authorization control distinguishes it from live beta behavior.

### Checkout reliability

No client request/idempotency key or durable checkout/order record was found.
Repeated clicks can create multiple Stripe sessions. Downstream clients have no
explicit connect/read timeouts, retry policy or circuit-breaker behavior.

### Sensitive logging

Logs include raw user IDs, pricing plan IDs, balances and checkout/session result
metadata. Payment diagnostics need a redaction and audit policy.

## Functional classification

| Capability | Result |
|---|---|
| Pricing | Working locally; commercial/version rules undefined |
| Wallet and transactions | Incomplete; identity is untrusted |
| Demo purchase | Unsafe outside isolated fixtures |
| Checkout | Incomplete; identity, idempotency and order lifecycle absent |
| Estimate | Incomplete; caller estimates and token-to-credit semantics unapproved |
| Clean build/contract | Absent but required |
| Health/operations | Basic health only |

The Payments epic contains the focused follow-up issues. All remain Backlog and no
issue was implemented during this audit.
