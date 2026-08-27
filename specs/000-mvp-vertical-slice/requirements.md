# MVP Vertical Slice Requirements

## Goal

Build the first offline SpendSense vertical slice:

```text
notification or sample input
  -> sanitized message
  -> financial classification
  -> deterministic parsing
  -> local persistence
  -> Compose display
```

## Functional Requirements

### R1 Notification Capture Boundary

The app must define a `NotificationListenerService` implementation that converts Android notifications into `RawNotification` and enqueues them for processing.

Acceptance criteria:

- The listener extracts package name, key, title, text, big text, sub text, and post time.
- The listener does not parse transactions synchronously.
- The listener delegates to `NotificationProcessor`.

### R2 Sanitization

The processor must normalize notification text before classification.

Acceptance criteria:

- Trims and normalizes whitespace.
- Removes duplicate lines.
- Removes common action text such as "tap to view" where safe.
- Normalizes common INR currency forms.
- Produces `SanitizedMessage`.

### R3 Sensitive Filter

The processor must reject sensitive authentication messages.

Acceptance criteria:

- OTP messages are ignored.
- Verification-code and login-code messages are ignored.
- CVV and password-reset messages are ignored.
- Ignored messages are not parsed or persisted.

### R4 Heuristic Financial Classifier

The app must classify sanitized messages without an LLM.

Acceptance criteria:

- Transaction keywords such as debited, credited, paid, spent, UPI, INR, Rs., and rupee symbol classify as financial.
- Ads, OTPs, and unrelated notifications classify as non-transaction or ignored.
- Classification returns a type and confidence.

### R5 Generic Parser

The app must parse common transaction fields from simple Indian financial messages.

Acceptance criteria:

- Extracts transaction type.
- Extracts amount in minor units.
- Extracts INR currency.
- Extracts merchant when present after "to" or "at".
- Extracts account last four digits when present.
- Extracts payment method for UPI and card messages.
- Produces field-level and overall confidence.

### R6 Persistence

The app must persist structured transactions in Room through a repository.

Acceptance criteria:

- UI observes transactions via `Flow`.
- Manual seed transactions can be inserted for development.
- Parser-created transactions are stored only after validation.

### R7 UI

The app must show persisted transactions in Compose.

Acceptance criteria:

- Home shows current month spending and recent transactions.
- Transactions screen shows a list of persisted transactions.
- Settings includes notification-access status or entry point.

## Non-Functional Requirements

- Core flow works offline.
- No LLM required.
- No raw notification text persisted in production code paths.
- No `Double` or `Float` used for persisted money.
- Parser tests use a transaction corpus.
