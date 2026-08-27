# Security And Privacy Specification

## Privacy Baseline

SpendSense is offline-first. Core transaction capture, parsing, storage, search, and dashboard display must not require network access.

## Sensitive Message Filtering

Messages matching the following categories must be ignored before classification, parsing, logging, storage, or AI fallback:

- OTP.
- Verification code.
- Login code.
- CVV.
- Password reset.
- Authentication request.

Expected decision:

```kotlin
ProcessingDecision.Ignore(reason = SensitiveContent.OTP)
```

## Raw Notification Retention

Production default:

```text
raw notification retention = 0
```

Allowed production persistence:

- Structured transaction fields.
- Evidence metadata.
- Hash of sanitized text.
- Parser name and confidence bucket.

Disallowed production persistence:

- Full raw notification text.
- Full sanitized notification text.
- Account numbers beyond safe suffix references.
- UPI IDs unless explicitly modeled and protected.
- Reference numbers in plaintext.

## Logging Rules

Allowed:

```kotlin
Logger.debug("Transaction parsed id=${transaction.id}")
```

Disallowed:

```kotlin
Logger.debug("Parsed ${notification.text}")
```

Crash reporting and analytics must never include notification text, merchant descriptions containing account data, account numbers, UPI IDs, reference numbers, or full transaction details.

## Encryption

The persistent database contains sensitive financial records and must be encrypted before production release.

Target design:

```text
Android Keystore
  -> non-exportable key material
  -> database encryption key
  -> encrypted Room database
```

For early local development, unencrypted Room is acceptable only if tracked as a pre-release security gap.

## AI Safety

Assume local LLM output is untrusted.

Required validation:

- Extracted amount must appear in the source message in equivalent normalized form.
- Currency must be supported and explicit or safely defaulted by source locale rules.
- Merchant must come from source text or known resolver evidence.
- Unknown fields must remain null.
- LLM output cannot write directly to the database.
