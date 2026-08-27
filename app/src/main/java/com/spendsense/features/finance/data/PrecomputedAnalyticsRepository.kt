package com.spendsense.features.finance.data

import com.spendsense.features.finance.domain.AnalyticsRepository
import com.spendsense.features.finance.domain.AnalyticsSummary
import com.spendsense.features.finance.domain.FinancialAnalyticsEngine
import com.spendsense.features.finance.domain.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PrecomputedAnalyticsRepository(
    transactionRepository: TransactionRepository,
    appScope: CoroutineScope,
    analyticsEngine: FinancialAnalyticsEngine = FinancialAnalyticsEngine(),
) : AnalyticsRepository {
    private val emptySummary = analyticsEngine.summarize(emptyList())

    private val summary = transactionRepository.observeTransactions()
        .map { transactions -> analyticsEngine.summarize(transactions) }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySummary,
        )

    override fun observeSummary(): Flow<AnalyticsSummary> = summary

    override suspend fun currentSummary(): AnalyticsSummary = summary.first()
}
