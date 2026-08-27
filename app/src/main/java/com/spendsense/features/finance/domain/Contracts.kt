package com.spendsense.features.finance.domain

import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    suspend fun insert(transaction: Transaction)
    suspend fun delete(id: String)
}

interface AnalyticsRepository {
    fun observeSummary(): Flow<AnalyticsSummary>
    suspend fun currentSummary(): AnalyticsSummary
}

interface SpendingGoalRepository {
    fun observeGoals(): Flow<List<SpendingGoal>>
    suspend fun insert(goal: SpendingGoal)
    suspend fun delete(id: String)
}

interface UserProfileRepository {
    fun observeProfile(): Flow<UserProfile>
    suspend fun update(profile: UserProfile)
}

interface FinancialMessageClassifier {
    suspend fun classify(message: SanitizedMessage): ClassificationResult
}

interface TransactionParser {
    suspend fun parse(message: SanitizedMessage): ParseResult
}

interface SensitiveMessageFilter {
    fun inspect(message: SanitizedMessage): ProcessingDecision
}

interface NotificationSanitizer {
    fun sanitize(notification: RawNotification): SanitizedMessage
}

interface ExtractionValidator {
    fun validate(message: SanitizedMessage, extraction: TransactionExtraction): Boolean
}

interface NotificationProcessor {
    suspend fun process(notification: RawNotification): ProcessingResult
}

interface LocalLanguageModel {
    suspend fun isAvailable(): Boolean
    suspend fun status(): LlmStatus
    suspend fun prepare() = Unit
    suspend fun generate(request: LlmRequest): LlmResult
}
