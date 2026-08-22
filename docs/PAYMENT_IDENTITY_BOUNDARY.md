# Payment Gateway identity boundary

Payment Gateway accepts retained browser-facing payment calls only from the Job
Seeker Copilot BFF. Every `/api/v1/payment` request must contain exactly one
valid `X-Service-Token` and one valid `X-Payment-Owner`.

The BFF owns user authentication and must derive `X-Payment-Owner` from the
validated User Management session. Payment Gateway rejects missing, forged or
ambiguous service credentials, invalid owner context, and every legacy
caller-selected `X-User-Id` header.

`BFF_TO_PAYMENT_GATEWAY_TOKEN` authenticates the inbound BFF. Payment Gateway
uses distinct `PAYMENT_GATEWAY_TO_PAYMENT_SERVICE_TOKEN` and
`PAYMENT_GATEWAY_TO_STRIPE_GATEWAY_TOKEN` credentials downstream. Owner-scoped
Payment Service and Stripe checkout calls carry the same trusted
`X-Payment-Owner`; browser credentials are never forwarded.

All three tokens must contain at least 32 bytes, must be mutually distinct, and
are compared in constant time on ingress. The payment client route remains
beta-disabled until the wider payments epic explicitly enables it.
