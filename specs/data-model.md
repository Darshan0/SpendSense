# Data Model Specification

## Money

All money is represented as integer minor units.

Examples:

```text
Rs. 428.00 -> 42800
Rs. 428.55 -> 42855
```

Never use `Double` or `Float` for stored money.

## Core Domain Entities

```kotlin
data class Transaction(
    val id: String,
    val type: TransactionType,
    val status: TransactionStatus,
    val amountMinor: Long,
    val currency: String,
    val merchant: Merchant?,
    val category: TransactionCategory,
    val paymentMethod: PaymentMethod?,
    val account: AccountReference?,
    val transactionTime: Instant?,
    val source: TransactionSource,
    val referenceNumber: String?,
    val availableBalanceMinor: Long?,
    val confidence: Float,
    val verificationStatus: VerificationStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)
```

```kotlin
enum class TransactionType {
    DEBIT,
    CREDIT,
    REFUND,
    CASH_WITHDRAWAL,
    TRANSFER,
    PAYMENT_FAILED,
    REVERSAL,
    UNKNOWN
}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REVERSED
}

enum class TransactionCategory {
    FOOD,
    GROCERIES,
    TRANSPORT,
    FUEL,
    SHOPPING,
    ENTERTAINMENT,
    UTILITIES,
    RENT,
    HEALTHCARE,
    EDUCATION,
    TRAVEL,
    SUBSCRIPTION,
    INVESTMENT,
    SALARY,
    TRANSFER,
    CASH,
    OTHER
}
```

## Notification Models

```kotlin
data class RawNotification(
    val key: String,
    val packageName: String,
    val title: String?,
    val text: String?,
    val bigText: String?,
    val subText: String?,
    val postedAt: Long
)

data class SanitizedMessage(
    val sourcePackage: String,
    val sender: String?,
    val text: String,
    val receivedAt: Instant
)
```

## Evidence

Transactions and evidence are separate.

```kotlin
data class TransactionEvidence(
    val id: String,
    val transactionId: String,
    val sourcePackage: String,
    val sourceType: EvidenceType,
    val timestamp: Instant,
    val textHash: String
)
```

Production behavior stores hashes and metadata, not raw text.

## Room Tables

Initial tables:

- `transactions`
- `accounts`
- `merchants`
- `merchant_aliases`
- `transaction_evidence`
- `merchant_preferences`
- `user_corrections`

Later tables:

- `budgets`
- `recurring_payments`
- `categories`

## Repository Contract

```kotlin
interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    fun observeTransaction(id: String): Flow<Transaction?>
    suspend fun insert(transaction: Transaction)
    suspend fun update(transaction: Transaction)
    suspend fun delete(id: String)
}
```
