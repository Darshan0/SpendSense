# Product Specification

## Product

SpendSense is an Android-first, offline-first personal finance app that detects financial transactions from phone notifications and converts them into structured local transaction records.

The app must preserve financial correctness, privacy, and battery life by using deterministic parsing before any on-device AI fallback.

## Core Principle

Financial correctness must not depend entirely on an LLM.

The source of truth is:

```text
validated notifications
rules
validators
database
user corrections
```

The LLM, when introduced later, is only a fallback parser or explanation layer.

## MVP Scope

The MVP includes:

- Android notification ingestion.
- Notification sanitization.
- Sensitive-content filtering for OTP, authentication, CVV, and password reset messages.
- Heuristic financial message classification.
- Deterministic transaction parsing for common Indian payment messages.
- Room-backed transaction storage.
- Compose dashboard and transaction list.
- Category assignment from merchant rules and user corrections.
- Low-confidence transaction verification UI.

## Explicit Non-Goals For MVP

- Cloud sync.
- Backend APIs.
- iOS.
- Fine-tuning.
- RAG.
- Knowledge graph.
- Forecasting.
- Subscription detection.
- AI assistant.
- Gemini Nano or LiteRT production integration.

## Personas

- Privacy-conscious user who wants a local spend tracker.
- Indian digital-payment user receiving bank, UPI, wallet, and card notifications.
- User who wants automatic tracking but must be able to correct mistakes.

## Primary User Journeys

1. User grants notification access after seeing a privacy explanation.
2. A transaction notification arrives.
3. SpendSense detects whether it is financial and not sensitive.
4. SpendSense extracts amount, type, merchant, account suffix, payment method, and timestamp.
5. High-confidence transactions are saved automatically.
6. Low-confidence transactions are shown for verification.
7. User corrections are remembered for future categorization.
8. User views monthly spending and recent transactions.

## Product Acceptance Criteria

- The app works without network access for capture, parsing, storage, and dashboard display.
- Monetary values are always stored as integer minor units.
- OTP and authentication messages are ignored before any AI pipeline.
- Raw notification text is not persisted by default in production behavior.
- The notification listener performs no expensive parsing or model work synchronously.
- The UI never requires an LLM to show transactions or analytics.
- A manually inserted transaction can appear in the dashboard before notification ingestion is fully enabled.
