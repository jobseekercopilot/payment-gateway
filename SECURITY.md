# Security policy

This repository is not ready to handle private-beta payment traffic.

Report suspected vulnerabilities privately to the repository owner. Do not put
Stripe secrets, webhook material, user identifiers, checkout/session identifiers,
balances, transaction histories or exploit details in ordinary issues or logs.

The audit found that browser-controlled `X-User-Id` values are treated as identity
without token validation. The current private client deliberately blocks payment
proxy routes; retain that boundary until authenticated ownership and downstream
service authorization are proven.

Rotate any exposed secret in the relevant provider/system. Removing a Git commit
does not revoke a credential.
