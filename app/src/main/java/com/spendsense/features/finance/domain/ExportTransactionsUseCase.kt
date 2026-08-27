package com.spendsense.features.finance.domain

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ExportTransactionsUseCase {
    fun buildCsv(
        transactions: List<Transaction>,
        analytics: AnalyticsSummary,
    ): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH)
        val zone = ZoneId.systemDefault()
        val header = listOf(
            "Date",
            "Type",
            "Status",
            "Merchant",
            "Category",
            "Amount",
            "Currency",
            "Payment Method",
            "Account",
            "Confidence",
            "Category Share",
            "Recent 7 Day Category Spend",
            "Previous 7 Day Category Spend",
            "Recent Direction",
            "Advisor Note",
        )
        val rows = transactions.sortedByDescending { it.transactionTime ?: it.createdAt }.map { transaction ->
            val categorySpend = analytics.categorySpends.firstOrNull { it.category == transaction.category }?.amountMinor ?: 0L
            val categoryShare = percent(categorySpend, analytics.totalExpenseMinor)
            val trend = analytics.recentSpend.categoryTrends.firstOrNull { it.category == transaction.category }
            listOf(
                formatter.format((transaction.transactionTime ?: transaction.createdAt).atZone(zone)),
                transaction.type.name,
                transaction.status.name,
                transaction.merchantName ?: "Unknown merchant",
                transaction.category.displayName(),
                minorToMajor(transaction.amountMinor),
                transaction.currency,
                transaction.paymentMethod?.name.orEmpty(),
                transaction.accountLast4?.let { "****$it" }.orEmpty(),
                "${(transaction.confidence * 100).toInt()}%",
                "$categoryShare%",
                minorToMajor(trend?.current7DaysMinor ?: 0L),
                minorToMajor(trend?.previous7DaysMinor ?: 0L),
                trend?.direction?.name.orEmpty(),
                advisorNote(transaction.category, trend),
            )
        }

        return buildString {
            appendLine(header.toCsvLine())
            rows.forEach { appendLine(it.toCsvLine()) }
        }
    }

    private fun advisorNote(category: TransactionCategory, trend: CategoryTrend?): String {
        return when {
            trend?.direction == TrendDirection.UP && category.expenseRole() == ExpenseRole.DISCRETIONARY ->
                "${category.displayName()} is rising versus the previous 7 days; set a cap."
            trend?.direction == TrendDirection.DOWN && category.expenseRole() == ExpenseRole.DISCRETIONARY ->
                "${category.displayName()} is cooling down; keep the current limit."
            category == TransactionCategory.INVESTMENT && trend?.direction != TrendDirection.UP ->
                "Investment momentum is not increasing; protect a fixed transfer."
            category == TransactionCategory.RENT ->
                "Fixed cost; review only during lease or housing decisions."
            else -> "Track weekly movement before acting."
        }
    }

    private fun minorToMajor(amountMinor: Long): String = "%.2f".format(Locale.US, amountMinor / 100.0)

    private fun percent(numerator: Long, denominator: Long): Int {
        return if (denominator <= 0L) 0 else ((numerator * 100L) / denominator).toInt()
    }

    private fun List<String>.toCsvLine(): String = joinToString(",") { value ->
        "\"${value.replace("\"", "\"\"")}\""
    }
}
