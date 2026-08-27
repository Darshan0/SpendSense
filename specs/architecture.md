# Architecture Specification

## Platform

- Android first.
- Kotlin.
- Jetpack Compose.
- Room.
- Hilt.
- Coroutines and Flow.

## Folder Layout

Use the requested clean architecture folder structure inside the Android app source set:

```text
app/src/main/java/com/spendsense
core
  utils
  presentation
features
  ui
  data
  usecase
```

Responsibilities:

- `core/utils`: common deterministic helpers such as money parsing and text sanitization.
- `core/presentation`: shared theme, formatting, and reusable presentation helpers.
- `features/usecase`: business models, interfaces, and application use cases.
- `features/data`: Android adapters, repositories, parsers, classifiers, and persistence implementations.
- `features/ui`: Compose screens and user interaction.

The original TRD target can still be split into Gradle modules later:

```text
app
core:common
core:model
core:database
core:security
core:preferences
core:testing
ingestion:notification
intelligence:classifier
intelligence:parser
intelligence:rules
intelligence:merchant
intelligence:category
intelligence:deduplication
intelligence:reconciliation
intelligence:llm
feature:onboarding
feature:home
feature:transactions
feature:analytics
feature:budgets
feature:assistant
feature:settings
benchmark
```

For the first implementation, modules may be introduced incrementally, but package boundaries should already reflect the target architecture.

## Dependency Direction

Allowed direction:

```text
feature -> domain/use cases -> core:model
ingestion -> processing/domain -> core:model
intelligence -> core:model
core:database -> core:model
app -> all feature and composition modules
```

Disallowed:

- UI directly calls Android notification APIs.
- UI directly depends on database entities.
- Parser writes directly to Room.
- LLM writes directly to Room.
- Notification listener performs heavy parsing or model inference.

## Processing Pipeline

```text
StatusBarNotification
  -> RawNotification
  -> SanitizedMessage
  -> SensitiveMessageFilter
  -> FinancialMessageClassifier
  -> TransactionParser
  -> ExtractionValidator
  -> TransactionReconciler
  -> MerchantResolver
  -> CategoryEngine
  -> TransactionDeduplicator
  -> TransactionRepository
```

## Required Interfaces

```kotlin
interface NotificationProcessor {
    suspend fun process(notification: RawNotification)
}

interface FinancialMessageClassifier {
    suspend fun classify(message: SanitizedMessage): ClassificationResult
}

interface TransactionParser {
    suspend fun parse(message: SanitizedMessage): ParseResult
}

interface ExtractionValidator {
    fun validate(
        message: SanitizedMessage,
        extraction: TransactionExtraction
    ): ValidationResult
}

interface MerchantResolver {
    suspend fun resolve(rawMerchant: String): Merchant?
}

interface CategoryEngine {
    suspend fun categorize(transaction: ParsedTransaction): CategoryPrediction
}

interface TransactionDeduplicator {
    suspend fun findDuplicate(transaction: ParsedTransaction): Transaction?
}

interface LocalLanguageModel {
    suspend fun isAvailable(): Boolean
    suspend fun generate(request: LlmRequest): LlmResult
}
```

## Confidence Policy

- `confidence >= 0.90`: save automatically.
- `0.65 <= confidence < 0.90`: run AI extraction when available; otherwise request user verification.
- `confidence < 0.65`: request user verification after fallback parsing.

For MVP, these thresholds are local constants.

## ADR Index

- `adr/0001-deterministic-before-llm.md`
- `adr/0002-raw-notification-retention.md`
