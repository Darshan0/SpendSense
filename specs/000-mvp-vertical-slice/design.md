# MVP Vertical Slice Design

## Package Shape

Until Gradle modules are created, use package boundaries that can later move into modules:

```text
com.spendsense.core.model
com.spendsense.core.database
com.spendsense.ingestion.notification
com.spendsense.intelligence.classifier
com.spendsense.intelligence.parser
com.spendsense.intelligence.rules
com.spendsense.intelligence.merchant
com.spendsense.intelligence.category
com.spendsense.feature.home
com.spendsense.feature.transactions
com.spendsense.feature.settings
```

## Processing Flow

```text
LedgerNotificationListener.onNotificationPosted
  -> NotificationMapper.map
  -> NotificationProcessor.enqueue/process
  -> NotificationSanitizer.sanitize
  -> SensitiveMessageFilter.inspect
  -> HeuristicFinancialClassifier.classify
  -> CompositeTransactionParser.parse
  -> ExtractionValidator.validate
  -> TransactionRepository.insert
```

## Initial Implementations

- `LedgerNotificationListener`: Android service boundary.
- `NotificationMapper`: converts `StatusBarNotification` to `RawNotification`.
- `DefaultNotificationProcessor`: orchestrates the pipeline.
- `DefaultNotificationSanitizer`: deterministic text cleanup.
- `RegexSensitiveMessageFilter`: blocks OTP/authentication content.
- `HeuristicFinancialClassifier`: keyword classifier.
- `CompositeTransactionParser`: tries known-source parser, then generic parser.
- `GenericFinancialParser`: regex parser for amount, type, merchant, account suffix, payment method.
- `DefaultExtractionValidator`: verifies amount consistency and required fields.
- `NoOpLanguageModel`: returns unavailable.

## Parser Rules

Start with a small set of explicit patterns:

```text
(Rs.|INR|₹) amount debited ... A/c XXXX ... via UPI to merchant
amount paid to merchant
amount spent at merchant
amount credited ... A/c XXXX
refund of amount
transaction failed
```

Rules should be declared as data where practical rather than scattered through call sites.

## Confidence Calculation

Initial weights:

- Amount: 40%.
- Transaction type: 20%.
- Merchant: 15%.
- Account: 10%.
- Date/time: 10%.
- Payment method: 5%.

If a field is not present in the notification but not necessary for a valid transaction, its missing confidence should reduce overall confidence but not always fail parsing.

## Persistence

Initial Room entities:

- `TransactionEntity`.
- `MerchantEntity`.
- `AccountEntity`.
- `TransactionEvidenceEntity`.

Initial DAO:

- Observe all transactions sorted by `transaction_time` then `created_at`.
- Insert transaction.
- Update transaction.
- Delete transaction.

## UI

Navigation:

- Home.
- Transactions.
- Settings.

Home:

- Monthly spend total from deterministic query.
- Recent transaction list.

Transactions:

- Chronological list.
- Amount, merchant, category, date, and confidence state.

Settings:

- Notification access status.
- Privacy/retention text.

## Test Corpus

Create:

```text
test-data/synthetic/
test-data/hdfc/
test-data/icici/
test-data/sbi/
test-data/axis/
test-data/kotak/
test-data/gpay/
test-data/phonepe/
test-data/paytm/
```

Each sample should include input and expected structured output.
