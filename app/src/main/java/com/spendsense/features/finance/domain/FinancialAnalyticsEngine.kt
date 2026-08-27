package com.spendsense.features.finance.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

class FinancialAnalyticsEngine {
    fun summarize(transactions: List<Transaction>): AnalyticsSummary {
        val expenses = transactions.filter {
            it.status == TransactionStatus.COMPLETED &&
                (it.type == TransactionType.DEBIT || it.type == TransactionType.CASH_WITHDRAWAL)
        }
        val income = transactions.filter {
            it.status == TransactionStatus.COMPLETED &&
                (it.type == TransactionType.CREDIT || it.type == TransactionType.REFUND)
        }

        val totalExpense = expenses.sumOf { it.amountMinor }
        val totalIncome = income.sumOf { it.amountMinor }
        val categorySpends = expenses
            .groupBy { it.category }
            .map { (category, categoryTransactions) ->
                CategorySpend(category, categoryTransactions.sumOf { it.amountMinor })
            }
            .sortedByDescending { it.amountMinor }
        val merchantSpends = expenses
            .groupBy { it.merchantName ?: "Unknown merchant" }
            .map { (merchant, merchantTransactions) ->
                MerchantSpend(merchant, merchantTransactions.sumOf { it.amountMinor })
            }
            .sortedByDescending { it.amountMinor }

        val roleTotals = expenses
            .groupBy { it.category.expenseRole() }
            .mapValues { (_, roleTransactions) -> roleTransactions.sumOf { it.amountMinor } }
        val segments = ExpenseRole.entries.mapNotNull { role ->
            val amount = roleTotals[role] ?: 0L
            if (amount == 0L) {
                null
            } else {
                SpendingSegment(
                    role = role,
                    amountMinor = amount,
                    sharePercent = percent(amount, totalExpense),
                )
            }
        }.sortedByDescending { it.amountMinor }

        val fixedNeed = roleTotals[ExpenseRole.FIXED_NEED] ?: 0L
        val variableNeed = roleTotals[ExpenseRole.VARIABLE_NEED] ?: 0L
        val discretionary = roleTotals[ExpenseRole.DISCRETIONARY] ?: 0L
        val investment = roleTotals[ExpenseRole.SAVINGS_INVESTMENT] ?: 0L
        val spendableExpense = (totalExpense - investment).coerceAtLeast(0L)
        val netCashFlow = totalIncome - totalExpense
        val savingsRate = if (totalIncome <= 0L) 0 else percent(netCashFlow.coerceAtLeast(0L) + investment, totalIncome)
        val recentSpend = recentSpendSummary(expenses, totalIncome)

        return AnalyticsSummary(
            totalExpenseMinor = totalExpense,
            totalIncomeMinor = totalIncome,
            netCashFlowMinor = netCashFlow,
            categorySpends = categorySpends,
            merchantSpends = merchantSpends,
            topCategories = categorySpends.take(5),
            topMerchants = merchantSpends.take(5),
            topDiscretionaryMerchants = expenses
                .filter { it.category.isDiscretionary() }
                .groupBy { it.merchantName ?: "Unknown merchant" }
                .map { (merchant, merchantTransactions) ->
                    MerchantSpend(merchant, merchantTransactions.sumOf { it.amountMinor })
                }
                .sortedByDescending { it.amountMinor }
                .take(5),
            transactionCount = transactions.size,
            spendingSegments = segments,
            budgetHealth = budgetHealth(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                spendableExpense = spendableExpense,
                fixedNeed = fixedNeed,
                discretionary = discretionary,
                savingsRate = savingsRate,
            ),
            cashFlowAssessment = CashFlowAssessment(
                expenseRatioPercent = percent(totalExpense, totalIncome),
                savingsRatePercent = savingsRate,
                fixedCostRatioPercent = percent(fixedNeed, totalIncome),
                discretionaryRatioPercent = percent(discretionary, totalIncome),
                investmentRatePercent = percent(investment, totalIncome),
            ),
            savingsOpportunities = savingsOpportunities(categorySpends, merchantSpends, totalExpense),
            categoryRecommendations = categoryRecommendations(categorySpends, totalExpense),
            recentSpend = recentSpend,
        )
    }

    private fun recentSpendSummary(
        expenses: List<Transaction>,
        totalIncome: Long,
        now: Instant = Instant.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): RecentSpendSummary {
        val today = now.atZone(zoneId).toLocalDate()
        val last7Days = (6L downTo 0L).map { daysAgo -> today.minusDays(daysAgo) }
        val previous7Days = (13L downTo 7L).map { daysAgo -> today.minusDays(daysAgo) }
        val expensesByDate = expenses.groupBy { transaction ->
            (transaction.transactionTime ?: transaction.createdAt).atZone(zoneId).toLocalDate()
        }
        val recentExpenses = last7Days.flatMap { date -> expensesByDate[date].orEmpty() }
        val previousExpenses = previous7Days.flatMap { date -> expensesByDate[date].orEmpty() }
        val todaySpend = expensesByDate[today].orEmpty().sumOf { it.amountMinor }
        val yesterdaySpend = expensesByDate[today.minusDays(1)].orEmpty().sumOf { it.amountMinor }
        val last7DaysSpend = recentExpenses.sumOf { it.amountMinor }
        val dailyAverage = last7DaysSpend / 7L
        val suggestedDailyBudget = if (totalIncome > 0L) (totalIncome * 70L) / 3_000L else 0L
        val categoryTrends = categoryTrends(recentExpenses, previousExpenses)
        val previousInvestment = previousExpenses
            .filter { it.category == TransactionCategory.INVESTMENT }
            .sumOf { it.amountMinor }

        return RecentSpendSummary(
            todaySpendMinor = todaySpend,
            yesterdaySpendMinor = yesterdaySpend,
            last7DaysSpendMinor = last7DaysSpend,
            dailyAverageMinor = dailyAverage,
            suggestedDailyBudgetMinor = suggestedDailyBudget,
            topRecentCategories = recentExpenses
                .groupBy { it.category }
                .map { (category, transactions) -> CategorySpend(category, transactions.sumOf { it.amountMinor }) }
                .sortedByDescending { it.amountMinor }
                .take(5),
            topRecentMerchants = recentExpenses
                .groupBy { it.merchantName ?: "Unknown merchant" }
                .map { (merchant, transactions) -> MerchantSpend(merchant, transactions.sumOf { it.amountMinor }) }
                .sortedByDescending { it.amountMinor }
                .take(5),
            dailyTrend = last7Days.map { date ->
                DailySpend(
                    label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    amountMinor = expensesByDate[date].orEmpty().sumOf { it.amountMinor },
                )
            },
            categoryTrends = categoryTrends,
            investmentTrend = categoryTrends.firstOrNull { it.category == TransactionCategory.INVESTMENT }
                ?: CategoryTrend(
                    category = TransactionCategory.INVESTMENT,
                    current7DaysMinor = 0L,
                    previous7DaysMinor = previousInvestment,
                    deltaMinor = -previousInvestment,
                    deltaPercent = if (previousInvestment > 0L) -100 else 0,
                    direction = if (previousInvestment > 0L) TrendDirection.DOWN else TrendDirection.FLAT,
                ),
        )
    }

    private fun categoryTrends(
        currentExpenses: List<Transaction>,
        previousExpenses: List<Transaction>,
    ): List<CategoryTrend> {
        val currentByCategory = currentExpenses
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amountMinor } }
        val previousByCategory = previousExpenses
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amountMinor } }

        return (currentByCategory.keys + previousByCategory.keys)
            .map { category ->
                val current = currentByCategory[category] ?: 0L
                val previous = previousByCategory[category] ?: 0L
                val delta = current - previous
                CategoryTrend(
                    category = category,
                    current7DaysMinor = current,
                    previous7DaysMinor = previous,
                    deltaMinor = delta,
                    deltaPercent = trendPercent(delta, previous, current),
                    direction = when {
                        delta > 0L -> TrendDirection.UP
                        delta < 0L -> TrendDirection.DOWN
                        else -> TrendDirection.FLAT
                    },
                )
            }
            .sortedWith(
                compareByDescending<CategoryTrend> { abs(it.deltaMinor) }
                    .thenByDescending { it.current7DaysMinor },
            )
    }

    private fun trendPercent(delta: Long, previous: Long, current: Long): Int {
        return when {
            previous > 0L -> ((delta * 100L) / previous).toInt()
            current > 0L -> 100
            else -> 0
        }
    }

    private fun budgetHealth(
        totalIncome: Long,
        totalExpense: Long,
        spendableExpense: Long,
        fixedNeed: Long,
        discretionary: Long,
        savingsRate: Int,
    ): BudgetHealth {
        if (totalIncome <= 0L && totalExpense <= 0L) {
            return BudgetHealth(
                score = 0,
                status = "Not enough data",
                explanation = "Capture income and expenses before scoring the budget.",
            )
        }

        var score = 70
        val expenseRatio = percent(totalExpense, totalIncome)
        val spendableRatio = percent(spendableExpense, totalIncome)
        val fixedRatio = percent(fixedNeed, totalIncome)
        val discretionaryRatio = percent(discretionary, totalIncome)

        score += when {
            savingsRate >= 30 -> 18
            savingsRate >= 20 -> 12
            savingsRate >= 10 -> 6
            savingsRate > 0 -> 2
            else -> -22
        }
        score += when {
            spendableRatio <= 55 -> 10
            spendableRatio <= 70 -> 3
            spendableRatio <= 85 -> -8
            else -> -18
        }
        score += when {
            fixedRatio <= 35 -> 6
            fixedRatio <= 50 -> 0
            else -> -10
        }
        score += when {
            discretionaryRatio <= 20 -> 6
            discretionaryRatio <= 35 -> 0
            else -> -8
        }

        val boundedScore = score.coerceIn(0, 100)
        val status = when {
            boundedScore >= 82 -> "Strong"
            boundedScore >= 65 -> "Stable"
            boundedScore >= 45 -> "Needs attention"
            else -> "At risk"
        }
        val explanation = when {
            savingsRate >= 20 && spendableRatio <= 70 -> "You are preserving a healthy savings buffer while keeping spendable expenses controlled."
            savingsRate <= 0 -> "Tracked outflows are using all tracked income, so the next move is to cut discretionary spend or pause non-essential purchases."
            fixedRatio > 50 -> "Fixed commitments are taking more than half of tracked income, leaving less room to adapt."
            discretionaryRatio > 35 -> "Discretionary categories are high enough to become the first place to optimize."
            else -> "Your budget is workable, but one or two category caps would make goal progress more predictable."
        }

        return BudgetHealth(
            score = boundedScore,
            status = status,
            explanation = "$explanation Expense ratio is $expenseRatio%.",
        )
    }

    private fun savingsOpportunities(
        categorySpends: List<CategorySpend>,
        merchantSpends: List<MerchantSpend>,
        totalExpense: Long,
    ): List<SavingsOpportunity> {
        val categoryOpportunities = categorySpends
            .filter { it.category.isDiscretionary() && it.amountMinor > 0L }
            .take(4)
            .map { spend ->
                val cutPercent = spend.category.cutPercent()
                SavingsOpportunity(
                    title = "Trim ${spend.category.displayName()}",
                    category = spend.category,
                    merchantName = null,
                    currentAmountMinor = spend.amountMinor,
                    suggestedCutPercent = cutPercent,
                    potentialSavingMinor = (spend.amountMinor * cutPercent) / 100L,
                    rationale = "${spend.category.displayName()} is ${percent(spend.amountMinor, totalExpense)}% of tracked outflows and is flexible enough to cap.",
                )
            }

        val merchantOpportunity = merchantSpends.firstOrNull()?.let { merchant ->
            SavingsOpportunity(
                title = "Review ${merchant.merchantName}",
                category = null,
                merchantName = merchant.merchantName,
                currentAmountMinor = merchant.amountMinor,
                suggestedCutPercent = 10,
                potentialSavingMinor = (merchant.amountMinor * 10L) / 100L,
                rationale = "This merchant is one of the largest tracked outflows, so even a small cap has visible impact.",
            )
        }

        return (categoryOpportunities + listOfNotNull(merchantOpportunity))
            .filter { it.potentialSavingMinor > 0L }
            .sortedByDescending { it.potentialSavingMinor }
            .take(5)
    }

    private fun categoryRecommendations(
        categorySpends: List<CategorySpend>,
        totalExpense: Long,
    ): List<CategoryRecommendation> {
        return categorySpends.map { spend ->
            val role = spend.category.expenseRole()
            val share = percent(spend.amountMinor, totalExpense)
            val cap = if (role == ExpenseRole.DISCRETIONARY) {
                spend.amountMinor - ((spend.amountMinor * spend.category.cutPercent()) / 100L)
            } else {
                null
            }
            CategoryRecommendation(
                category = spend.category,
                role = role,
                amountMinor = spend.amountMinor,
                sharePercent = share,
                action = spend.category.recommendedAction(role, share),
                suggestedCapMinor = cap,
            )
        }
    }

    private fun percent(numerator: Long, denominator: Long): Int {
        return if (denominator <= 0L) 0 else ((numerator * 100L) / denominator).toInt()
    }
}

fun TransactionCategory.expenseRole(): ExpenseRole {
    return when (this) {
        TransactionCategory.RENT,
        TransactionCategory.UTILITIES -> ExpenseRole.FIXED_NEED
        TransactionCategory.GROCERIES,
        TransactionCategory.TRANSPORT,
        TransactionCategory.FUEL,
        TransactionCategory.HEALTHCARE,
        TransactionCategory.EDUCATION -> ExpenseRole.VARIABLE_NEED
        TransactionCategory.FOOD,
        TransactionCategory.SHOPPING,
        TransactionCategory.ENTERTAINMENT,
        TransactionCategory.TRAVEL,
        TransactionCategory.SUBSCRIPTION,
        TransactionCategory.OTHER -> ExpenseRole.DISCRETIONARY
        TransactionCategory.INVESTMENT -> ExpenseRole.SAVINGS_INVESTMENT
        TransactionCategory.SALARY,
        TransactionCategory.TRANSFER,
        TransactionCategory.CASH -> ExpenseRole.TRANSFER
    }
}

fun TransactionCategory.isDiscretionary(): Boolean {
    return expenseRole() == ExpenseRole.DISCRETIONARY
}

fun TransactionCategory.displayName(): String {
    return name.lowercase().replaceFirstChar { it.titlecase() }
}

private fun TransactionCategory.cutPercent(): Int {
    return when (this) {
        TransactionCategory.FOOD,
        TransactionCategory.ENTERTAINMENT,
        TransactionCategory.SHOPPING -> 20
        TransactionCategory.SUBSCRIPTION,
        TransactionCategory.TRAVEL -> 15
        else -> 10
    }
}

private fun TransactionCategory.recommendedAction(role: ExpenseRole, share: Int): String {
    return when (role) {
        ExpenseRole.FIXED_NEED -> if (share >= 45) {
            "Keep paid, but review if this fixed cost can be renegotiated."
        } else {
            "Treat as a core bill and avoid cutting this first."
        }
        ExpenseRole.VARIABLE_NEED -> "Set a practical cap and compare weekly, because this is necessary but controllable."
        ExpenseRole.DISCRETIONARY -> "Put this category on a cap and redirect the saved amount to the top goal."
        ExpenseRole.SAVINGS_INVESTMENT -> "Protect this if cash flow stays positive; it already supports future goals."
        ExpenseRole.TRANSFER -> "Classify transfers separately so they do not distort lifestyle spending."
    }
}
