package com.spendsense.features.finance.domain

import java.time.Instant
import java.util.UUID

data class RawNotification(
    val key: String,
    val packageName: String,
    val title: String?,
    val text: String?,
    val bigText: String?,
    val subText: String?,
    val postedAt: Long,
)

data class SanitizedMessage(
    val sourcePackage: String,
    val sender: String?,
    val text: String,
    val receivedAt: Instant,
)

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val type: TransactionType,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val amountMinor: Long,
    val currency: String = "INR",
    val merchantName: String?,
    val category: TransactionCategory = TransactionCategory.OTHER,
    val paymentMethod: PaymentMethod?,
    val accountLast4: String?,
    val transactionTime: Instant?,
    val sourcePackage: String,
    val confidence: Float,
    val verificationStatus: VerificationStatus,
    val createdAt: Instant = Instant.now(),
)

data class SpendingGoal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmountMinor: Long,
    val currentAmountMinor: Long,
    val currency: String = "INR",
    val targetDateEpochMillis: Long?,
    val createdAt: Instant = Instant.now(),
)

data class UserProfile(
    val name: String = "Darshan",
    val monthlySalaryMinor: Long = 0L,
    val currency: String = "INR",
    val profilePhotoPath: String? = null,
)

data class CategorySpend(
    val category: TransactionCategory,
    val amountMinor: Long,
)

data class MerchantSpend(
    val merchantName: String,
    val amountMinor: Long,
)

enum class ExpenseRole {
    FIXED_NEED,
    VARIABLE_NEED,
    DISCRETIONARY,
    SAVINGS_INVESTMENT,
    TRANSFER,
}

data class SpendingSegment(
    val role: ExpenseRole,
    val amountMinor: Long,
    val sharePercent: Int,
)

data class BudgetHealth(
    val score: Int,
    val status: String,
    val explanation: String,
)

data class CashFlowAssessment(
    val expenseRatioPercent: Int,
    val savingsRatePercent: Int,
    val fixedCostRatioPercent: Int,
    val discretionaryRatioPercent: Int,
    val investmentRatePercent: Int,
)

data class SavingsOpportunity(
    val title: String,
    val category: TransactionCategory?,
    val merchantName: String?,
    val currentAmountMinor: Long,
    val suggestedCutPercent: Int,
    val potentialSavingMinor: Long,
    val rationale: String,
)

data class CategoryRecommendation(
    val category: TransactionCategory,
    val role: ExpenseRole,
    val amountMinor: Long,
    val sharePercent: Int,
    val action: String,
    val suggestedCapMinor: Long?,
)

data class DailySpend(
    val label: String,
    val amountMinor: Long,
)

enum class TrendDirection {
    UP,
    DOWN,
    FLAT,
}

data class CategoryTrend(
    val category: TransactionCategory,
    val current7DaysMinor: Long,
    val previous7DaysMinor: Long,
    val deltaMinor: Long,
    val deltaPercent: Int,
    val direction: TrendDirection,
)

data class RecentSpendSummary(
    val todaySpendMinor: Long,
    val yesterdaySpendMinor: Long,
    val last7DaysSpendMinor: Long,
    val dailyAverageMinor: Long,
    val suggestedDailyBudgetMinor: Long,
    val topRecentCategories: List<CategorySpend> = emptyList(),
    val topRecentMerchants: List<MerchantSpend> = emptyList(),
    val dailyTrend: List<DailySpend> = emptyList(),
    val categoryTrends: List<CategoryTrend> = emptyList(),
    val investmentTrend: CategoryTrend? = null,
)

data class AnalyticsSummary(
    val totalExpenseMinor: Long,
    val totalIncomeMinor: Long,
    val netCashFlowMinor: Long,
    val categorySpends: List<CategorySpend> = emptyList(),
    val merchantSpends: List<MerchantSpend> = emptyList(),
    val topCategories: List<CategorySpend>,
    val topMerchants: List<MerchantSpend>,
    val topDiscretionaryMerchants: List<MerchantSpend> = emptyList(),
    val transactionCount: Int,
    val spendingSegments: List<SpendingSegment> = emptyList(),
    val budgetHealth: BudgetHealth = BudgetHealth(
        score = 0,
        status = "Not enough data",
        explanation = "Capture more completed transactions before scoring budget health.",
    ),
    val cashFlowAssessment: CashFlowAssessment = CashFlowAssessment(
        expenseRatioPercent = 0,
        savingsRatePercent = 0,
        fixedCostRatioPercent = 0,
        discretionaryRatioPercent = 0,
        investmentRatePercent = 0,
    ),
    val savingsOpportunities: List<SavingsOpportunity> = emptyList(),
    val categoryRecommendations: List<CategoryRecommendation> = emptyList(),
    val recentSpend: RecentSpendSummary = RecentSpendSummary(
        todaySpendMinor = 0,
        yesterdaySpendMinor = 0,
        last7DaysSpendMinor = 0,
        dailyAverageMinor = 0,
        suggestedDailyBudgetMinor = 0,
    ),
)

data class FinancialInsight(
    val title: String,
    val body: String,
    val severity: InsightSeverity,
)

enum class InsightSeverity {
    INFO,
    OPPORTUNITY,
    WARNING,
}

data class AssistantMessage(
    val question: String,
    val answer: String,
)

data class ConversationTurn(
    val role: ConversationRole,
    val text: String,
)

enum class ConversationRole {
    USER,
    ASSISTANT,
}

enum class AssistantIntent {
    CATEGORY_TOTAL,
    MERCHANT_TOTAL,
    CUTBACK_PLAN,
    CASH_FLOW,
    GOAL_PLAN,
    TOP_SPEND,
    BUDGET_HEALTH,
    WHAT_IF,
    LOAN_PLANNING,
    EMERGENCY_FUND,
    RENT_BURDEN,
    TAX_PLANNING,
    CASUAL_REFLECTION,
    GENERAL_FINANCE_COACHING,
}

enum class PlanningType {
    PURCHASE_AFFORDABILITY,
    LOAN_AFFORDABILITY,
    EMERGENCY_FUND,
    RENT_BURDEN,
    GOAL_RUNWAY,
    TAX_READINESS,
    GENERAL_ADVISOR,
}

enum class PlanningVerdict {
    AFFORDABLE,
    CAUTION,
    DELAY,
    NEEDS_MORE_INFO,
    NOT_SUPPORTED_YET,
}

enum class PlanningConfidence {
    HIGH,
    MEDIUM,
    LOW,
}

data class PlanningFact(
    val label: String,
    val value: String,
)

data class PlanningRisk(
    val title: String,
    val severity: InsightSeverity,
    val detail: String,
)

data class ActionStep(
    val title: String,
    val detail: String,
)

data class PlanningResult(
    val type: PlanningType,
    val verdict: PlanningVerdict,
    val confidence: PlanningConfidence,
    val summary: String,
    val facts: List<PlanningFact> = emptyList(),
    val risks: List<PlanningRisk> = emptyList(),
    val recommendations: List<ActionStep> = emptyList(),
    val assumptions: List<String> = emptyList(),
    val missingInputs: List<String> = emptyList(),
)

data class LlmRequest(
    val prompt: String,
    val context: String,
    val conversationHistory: List<ConversationTurn> = emptyList(),
    val intent: AssistantIntent = AssistantIntent.GENERAL_FINANCE_COACHING,
)

data class LlmResult(
    val text: String,
    val provider: String,
    val usedFallback: Boolean,
)

data class LlmStatus(
    val provider: String,
    val state: LlmAvailabilityState,
    val detail: String,
)

enum class LlmAvailabilityState {
    AVAILABLE,
    DOWNLOADABLE,
    DOWNLOADING,
    UNAVAILABLE,
    NOT_INSTALLED,
    ERROR,
}

enum class TransactionType {
    DEBIT,
    CREDIT,
    REFUND,
    CASH_WITHDRAWAL,
    TRANSFER,
    PAYMENT_FAILED,
    REVERSAL,
    UNKNOWN,
}

enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REVERSED,
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
    OTHER,
}

enum class PaymentMethod {
    UPI,
    CARD,
    NET_BANKING,
    CASH,
    UNKNOWN,
}

enum class VerificationStatus {
    AUTO_VERIFIED,
    NEEDS_REVIEW,
    USER_VERIFIED,
}

enum class MessageType {
    TRANSACTION,
    BALANCE,
    OTP,
    ADVERTISEMENT,
    STATEMENT,
    REMINDER,
    NON_FINANCIAL,
    UNKNOWN,
}

enum class SensitiveContent {
    OTP,
    AUTHENTICATION,
    CVV,
    PASSWORD_RESET,
}

sealed interface ProcessingDecision {
    data object Continue : ProcessingDecision
    data class Ignore(val reason: SensitiveContent) : ProcessingDecision
}

data class ClassificationResult(
    val type: MessageType,
    val confidence: Float,
)

data class ExtractedField<T>(
    val value: T?,
    val confidence: Float,
    val source: ExtractionSource,
)

enum class ExtractionSource {
    REGEX,
    RULE,
    DEFAULT,
    UNKNOWN,
}

data class TransactionExtraction(
    val type: ExtractedField<TransactionType>,
    val amountMinor: ExtractedField<Long>,
    val currency: ExtractedField<String>,
    val merchantName: ExtractedField<String>,
    val paymentMethod: ExtractedField<PaymentMethod>,
    val accountLast4: ExtractedField<String>,
    val confidence: Float,
)

sealed interface ParseResult {
    data class Parsed(val extraction: TransactionExtraction) : ParseResult
    data class Failed(val reason: String) : ParseResult
}

sealed interface ProcessingResult {
    data class Saved(val transaction: Transaction) : ProcessingResult
    data class Ignored(val reason: String) : ProcessingResult
    data class NeedsReview(val extraction: TransactionExtraction) : ProcessingResult
    data class Failed(val reason: String) : ProcessingResult
}
