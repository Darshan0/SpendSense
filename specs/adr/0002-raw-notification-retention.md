# ADR 0002: Raw Notification Retention

## Status

Accepted

## Context

Notification text may contain highly sensitive financial and authentication data.

## Decision

Production behavior will not persist raw or sanitized notification text. The system may persist structured transaction fields, evidence metadata, and hashes.

Debug builds may support explicit opt-in sanitized sample retention for parser development.

## Consequences

- Parser debugging requires a deliberate debug-only workflow.
- Deduplication must work from normalized transaction fields and hashes.
- Privacy risk is reduced substantially.
