# MVP Vertical Slice Tasks

## Status Legend

- `[ ]` Not started.
- `[~]` In progress.
- `[x]` Complete.

## Phase 0: Project Bootstrap

- [x] Create Android Gradle project using Kotlin and Compose.
- [x] Configure package name `com.spendsense`.
- [~] Add Hilt, Room, coroutines, lifecycle, navigation, and test dependencies.
- [ ] Add baseline static checks available in the chosen template.
- [ ] Verify the empty app builds.

## Phase 1: Domain Model

- [x] Add transaction, merchant, account, category, payment method, source, status, and verification models.
- [x] Add `RawNotification` and `SanitizedMessage`.
- [x] Add `ExtractedField`, `TransactionExtraction`, `ParseResult`, and confidence model.
- [x] Add tests for money parsing into minor units.

Verification:

- [ ] Unit tests pass for model helpers and money parsing.

## Phase 2: Persistence

- [x] Add Room entities and DAOs.
- [x] Add `TransactionRepository` interface and Room implementation.
- [x] Add development seed transaction path.
- [ ] Add repository tests.

Verification:

- [~] Manual seed transaction appears through repository `Flow`.

## Phase 3: Ingestion

- [x] Add `LedgerNotificationListener`.
- [x] Add `NotificationMapper`.
- [x] Add `NotificationProcessor` interface and default implementation.
- [x] Add manifest service declaration and notification-listener permission.
- [x] Add settings entry point for notification-access screen.

Verification:

- [ ] Listener delegates `RawNotification` without doing heavy work.
- [ ] App can detect whether notification access is enabled.

## Phase 4: Sanitization And Filtering

- [x] Add notification sanitizer.
- [x] Add sensitive-message filter.
- [ ] Add tests for OTP/authentication rejection.
- [ ] Add tests for noisy notification cleanup.

Verification:

- [ ] Sensitive messages are ignored before classifier/parser.
- [ ] Sanitized output excludes common action boilerplate.

## Phase 5: Classifier

- [x] Add `FinancialMessageClassifier`.
- [x] Add `HeuristicFinancialClassifier`.
- [x] Add classification result model.
- [ ] Add tests for transaction, balance, OTP, ad, and non-financial messages.

Verification:

- [ ] Financial messages are routed to parser.
- [ ] Non-financial messages are not persisted.

## Phase 6: Generic Parser

- [x] Add `TransactionParser` interface.
- [x] Add generic regex parser.
- [ ] Add rule abstraction for reusable transaction patterns.
- [x] Add amount, type, merchant, account suffix, and payment method extraction.
- [x] Add confidence calculation.
- [x] Add extraction validator.
- [ ] Add synthetic test corpus.

Verification:

- [ ] Parser handles representative UPI debit, card debit, credit, refund, failure, and balance messages.
- [ ] LLM path is not required.

## Phase 7: Compose UI

- [x] Add app navigation.
- [x] Add reusable main UI components.
- [x] Add Home screen with month total and recent transactions.
- [x] Add Transactions screen.
- [x] Add Settings screen.
- [~] Wire ViewModels to repository flows.

Verification:

- [ ] Manually seeded and parsed transactions render correctly.
- [ ] UI handles empty state.

## Phase 8: First End-To-End Slice

- [x] Add a debug-only sample notification input path.
- [ ] Process a synthetic transaction through the full pipeline.
- [ ] Persist validated transaction.
- [ ] Render transaction in UI.
- [ ] Confirm sensitive sample messages are ignored.

Verification:

- [ ] One command or documented manual flow demonstrates end-to-end behavior.
- [ ] No raw notification body is persisted by production repository paths.

## Deferred Specs

Create separate numbered specs after the MVP vertical slice:

- `001-bank-parser-corpus`
- `002-deduplication-and-evidence`
- `003-merchant-categorization-corrections`
- `004-low-confidence-verification-ui`
- `005-on-device-llm-abstraction`
- `006-deterministic-analytics`
