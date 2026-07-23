# Contributing

This is a private, proprietary repository.

1. Start from `develop` and use a focused branch for an approved issue.
2. Keep one issue and one concern per pull request.
3. Never commit credentials, Stripe secrets, generated JARs, payment data or real
   user fixtures.
4. Preserve the fail-closed payment feature boundary until the Payments epic is
   explicitly approved and its security dependencies are complete.
5. Run `mvn -B clean verify` and relevant contract/security checks before review.
6. Open a pull request into `develop`; do not push implementation work directly.

Report security concerns using `SECURITY.md`.
