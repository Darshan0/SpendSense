package com.spendsense.features.finance.domain

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

private fun formatMoneyMinor(amountMinor: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        currency = Currency.getInstance("INR")
        maximumFractionDigits = if (amountMinor % 100L == 0L) 0 else 2
    }
    return formatter.format(amountMinor / 100.0)
}

class ObserveAnalyticsSummaryUseCase(
    private val analyticsRepository: AnalyticsRepository,
) {
    fun observe(): Flow<AnalyticsSummary> {
        return analyticsRepository.observeSummary()
    }

    suspend fun current(): AnalyticsSummary = analyticsRepository.currentSummary()
}

class GenerateInsightsUseCase(
    private val analytics: ObserveAnalyticsSummaryUseCase,
    private val goals: SpendingGoalRepository,
) {
    fun observe(): Flow<List<FinancialInsight>> {
        return combine(analytics.observe(), goals.observeGoals()) { summary, currentGoals ->
            buildInsights(summary, currentGoals)
        }.distinctUntilChanged()
    }

    private fun buildInsights(
        summary: AnalyticsSummary,
        goals: List<SpendingGoal>,
    ): List<FinancialInsight> {
        val insights = mutableListOf<FinancialInsight>()

        if (summary.transactionCount > 0) {
            insights += FinancialInsight(
                title = "Budget health: ${summary.budgetHealth.status} (${summary.budgetHealth.score}/100)",
                body = summary.budgetHealth.explanation,
                severity = when {
                    summary.budgetHealth.score >= 75 -> InsightSeverity.OPPORTUNITY
                    summary.budgetHealth.score >= 55 -> InsightSeverity.INFO
                    else -> InsightSeverity.WARNING
                },
            )
        }

        val risingFlexibleCategory = summary.recentSpend.categoryTrends.firstOrNull {
            it.direction == TrendDirection.UP &&
                it.current7DaysMinor > 0L &&
                it.category.isDiscretionary()
        }
        if (risingFlexibleCategory != null) {
            insights += FinancialInsight(
                title = "${risingFlexibleCategory.category.displayName()} is rising",
                body = "${risingFlexibleCategory.category.displayName()} is up ${formatMoneyMinor(risingFlexibleCategory.deltaMinor)} versus the previous 7 days. Put a daily cap on this category before it delays a goal.",
                severity = InsightSeverity.WARNING,
            )
        } else {
            val topCategory = summary.topCategories.firstOrNull()
            if (topCategory != null) {
                val share = if (summary.totalExpenseMinor == 0L) {
                    0
                } else {
                    ((topCategory.amountMinor * 100) / summary.totalExpenseMinor).toInt()
                }
                insights += FinancialInsight(
                    title = "${topCategory.category.displayName()} leads your spending",
                    body = "$share% of tracked expenses are in ${topCategory.category.displayName().lowercase()} so far. Treat this as context, then act on the recent 7-day movement first.",
                    severity = if (share >= 40) InsightSeverity.WARNING else InsightSeverity.INFO,
                )
            }
        }

        val investmentTrend = summary.recentSpend.investmentTrend
        if (summary.totalIncomeMinor > 0L && (investmentTrend == null || investmentTrend.current7DaysMinor <= investmentTrend.previous7DaysMinor)) {
            insights += FinancialInsight(
                title = "Investments need protection",
                body = "Investment outflow is not increasing versus the previous 7 days. Keep a fixed transfer before flexible spends if your cash flow allows it.",
                severity = InsightSeverity.WARNING,
            )
        }

        summary.savingsOpportunities.firstOrNull()?.let { opportunity ->
            insights += FinancialInsight(
                title = opportunity.title,
                body = "${opportunity.rationale} A ${opportunity.suggestedCutPercent}% cut could free ${formatMoneyMinor(opportunity.potentialSavingMinor)}.",
                severity = InsightSeverity.OPPORTUNITY,
            )
        }

        if (summary.netCashFlowMinor > 0) {
            insights += FinancialInsight(
                title = "Positive cash flow",
                body = "You have more tracked income than expenses. Current savings rate is ${summary.cashFlowAssessment.savingsRatePercent}%, so this surplus can move into a goal.",
                severity = InsightSeverity.OPPORTUNITY,
            )
        } else if (summary.totalExpenseMinor > 0) {
            insights += FinancialInsight(
                title = "Expenses are ahead",
                body = "Tracked expenses currently exceed tracked income. Review the top merchants before setting new goals.",
                severity = InsightSeverity.WARNING,
            )
        }

        val activeGoal = goals.firstOrNull()
        if (activeGoal != null && activeGoal.targetAmountMinor > 0) {
            val progress = ((activeGoal.currentAmountMinor * 100) / activeGoal.targetAmountMinor).coerceAtMost(100)
            val bestOpportunity = summary.savingsOpportunities.firstOrNull()
            insights += FinancialInsight(
                title = "${activeGoal.name} is $progress% funded",
                body = if (bestOpportunity != null) {
                    "Redirecting ${formatMoneyMinor(bestOpportunity.potentialSavingMinor)} from ${bestOpportunity.title.removePrefix("Trim ")} each month would make this goal more predictable."
                } else {
                    "Keep this visible when deciding whether discretionary spending fits your plan."
                },
                severity = InsightSeverity.INFO,
            )
        }

        if (insights.isEmpty()) {
            insights += FinancialInsight(
                title = "Start with transaction capture",
                body = "Once SpendSense sees a few transactions, local analysis will explain patterns and goal tradeoffs.",
                severity = InsightSeverity.INFO,
            )
        }

        return insights
    }
}

class AskAssistantUseCase(
    private val analytics: ObserveAnalyticsSummaryUseCase,
    private val goals: SpendingGoalRepository,
    private val languageModel: LocalLanguageModel,
    private val planningEngine: FinancialPlanningEngine = FinancialPlanningEngine(),
) {
    suspend fun llmStatus(): LlmStatus = languageModel.status()

    fun observeSuggestedAnswer(question: String): Flow<AssistantMessage> {
        return combine(analytics.observe(), goals.observeGoals()) { summary, currentGoals ->
            AssistantMessage(
                question = question,
                answer = groundedAnswer(question, summary, currentGoals),
            )
        }
    }

    suspend fun ask(
        question: String,
        summary: AnalyticsSummary? = null,
        conversationHistory: List<ConversationTurn> = emptyList(),
    ): AssistantMessage {
        val currentSummary = summary ?: analytics.current()
        val currentGoals = goals.observeGoals().first()
        val intent = classifyIntent(question, currentSummary)
        val planningResult = planningEngine.plan(question, intent, currentSummary, currentGoals)
        val requiredAnswer = groundedAnswer(question, currentSummary, currentGoals, intent, planningResult)
        val context = buildContext(currentSummary, currentGoals, requiredAnswer, intent, planningResult)
        val result = runCatching {
            languageModel.generate(
                LlmRequest(
                    prompt = question,
                    context = context,
                    conversationHistory = conversationHistory.takeLast(8),
                    intent = intent,
                ),
            )
        }.getOrElse { error ->
            LlmResult(
                text = error.message.orEmpty(),
                provider = "Local analytics coach",
                usedFallback = true,
            )
        }

        val modelAnswer = result.text.trim()
        val useGroundedAnswer = result.usedFallback ||
            modelAnswer.isBlank() ||
            !isAcceptableModelAnswer(modelAnswer, requiredAnswer, currentSummary, intent, planningResult)
        val answer = if (useGroundedAnswer) requiredAnswer else modelAnswer
        val source = if (useGroundedAnswer) "Local analytics coach" else "${result.provider} on device"

        return AssistantMessage(
            question = question,
            answer = "$answer\n\nSource: $source",
        )
    }

    private fun groundedAnswer(
        question: String,
        summary: AnalyticsSummary,
        goals: List<SpendingGoal>,
        intent: AssistantIntent = classifyIntent(question, summary),
        planningResult: PlanningResult = planningEngine.plan(question, intent, summary, goals),
    ): String {
        val normalizedQuestion = question.lowercase()
        val requestedCategory = TransactionCategory.entries.firstOrNull { category ->
            val label = category.displayName().lowercase()
            normalizedQuestion.contains(label) || normalizedQuestion.contains(category.name.lowercase())
        }

        if (intent == AssistantIntent.CATEGORY_TOTAL && requestedCategory != null) {
            return categorySpendAnswer(requestedCategory, summary)
        }

        val requestedMerchant = summary.merchantSpends.firstOrNull { merchant ->
            normalizedQuestion.contains(merchant.merchantName.lowercase())
        }
        if (intent == AssistantIntent.MERCHANT_TOTAL && requestedMerchant != null) {
            return merchantSpendAnswer(requestedMerchant, summary)
        }

        return when (intent) {
            AssistantIntent.CATEGORY_TOTAL -> categorySpendAnswer(requestedCategory ?: TransactionCategory.OTHER, summary)
            AssistantIntent.MERCHANT_TOTAL -> requestedMerchant?.let { merchantSpendAnswer(it, summary) } ?: topSpendAnswer(summary)
            AssistantIntent.CUTBACK_PLAN -> groundedAdvice(summary, goals)
            AssistantIntent.CASH_FLOW -> cashFlowAnswer(summary, goals)
            AssistantIntent.GOAL_PLAN -> goalAnswer(summary, goals)
            AssistantIntent.TOP_SPEND -> topSpendAnswer(summary)
            AssistantIntent.BUDGET_HEALTH -> budgetHealthAnswer(summary, goals)
            AssistantIntent.WHAT_IF,
            AssistantIntent.LOAN_PLANNING,
            AssistantIntent.EMERGENCY_FUND,
            AssistantIntent.RENT_BURDEN,
            AssistantIntent.TAX_PLANNING -> planningAnswer(planningResult)
            AssistantIntent.CASUAL_REFLECTION -> casualReflectionAnswer(summary, goals)
            AssistantIntent.GENERAL_FINANCE_COACHING -> advisorOverviewAnswer(summary, goals)
        }
    }

    private fun categorySpendAnswer(category: TransactionCategory, summary: AnalyticsSummary): String {
        val spend = summary.categorySpends.firstOrNull { it.category == category }
        if (spend == null || spend.amountMinor == 0L) {
            return "I do not see any tracked ${category.displayName()} spending yet. Once transactions in that category are captured, I can break down the amount and share of expenses."
        }

        val share = if (summary.totalExpenseMinor == 0L) {
            0
        } else {
            ((spend.amountMinor * 100L) / summary.totalExpenseMinor).toInt()
        }
        val recommendation = summary.categoryRecommendations.firstOrNull { it.category == category }
        val capText = recommendation?.suggestedCapMinor?.let { " A good next cap is ${formatMoney(it)}." }.orEmpty()
        val action = recommendation?.action ?: "Review whether this category still fits your current goal."
        return "${category.displayName()} spend is ${formatMoney(spend.amountMinor)} across the local transactions I have tracked. That is $share% of your tracked expenses of ${formatMoney(summary.totalExpenseMinor)}.\n\n$action$capText"
    }

    private fun merchantSpendAnswer(merchant: MerchantSpend, summary: AnalyticsSummary): String {
        val share = if (summary.totalExpenseMinor == 0L) {
            0
        } else {
            ((merchant.amountMinor * 100L) / summary.totalExpenseMinor).toInt()
        }
        return "${merchant.merchantName} spend is ${formatMoney(merchant.amountMinor)}, which is $share% of your tracked expenses.\n\nUse this merchant as a review point if it is discretionary or recurring."
    }

    private fun cashFlowAnswer(summary: AnalyticsSummary, goals: List<SpendingGoal>): String {
        val activeGoal = goals.firstOrNull { it.targetAmountMinor > it.currentAmountMinor }
        return buildString {
            append("Your tracked income is ")
            append(formatMoney(summary.totalIncomeMinor))
            append(" and tracked expenses are ")
            append(formatMoney(summary.totalExpenseMinor))
            append(", leaving ")
            append(if (summary.netCashFlowMinor >= 0L) "a surplus of " else "a gap of ")
            append(formatMoney(kotlin.math.abs(summary.netCashFlowMinor)))
            append(".")

        if (activeGoal != null && summary.netCashFlowMinor > 0L) {
                append("\n\nA 20% allocation of that surplus is ")
                append(formatMoney((summary.netCashFlowMinor * 20L) / 100L))
                append(" toward \"")
                append(activeGoal.name)
                append("\".")
            }
            append("\n\nBudget health is ")
            append(summary.budgetHealth.status)
            append(" at ")
            append(summary.budgetHealth.score)
            append("/100. Savings rate is ")
            append(summary.cashFlowAssessment.savingsRatePercent)
            append("%.")
        }
    }

    private fun goalAnswer(summary: AnalyticsSummary, goals: List<SpendingGoal>): String {
        val goal = goals
            .filter { it.targetAmountMinor > it.currentAmountMinor }
            .maxByOrNull { it.targetAmountMinor - it.currentAmountMinor }
            ?: return "I do not see an active goal gap yet. Add a goal amount and current savings so I can calculate a path."

        val remaining = goal.targetAmountMinor - goal.currentAmountMinor
        val monthlyTransfer = if (summary.netCashFlowMinor > 0L) (summary.netCashFlowMinor * 20L) / 100L else 0L
        val topSaving = summary.savingsOpportunities.firstOrNull()
        val suggestedTransfer = maxOf(monthlyTransfer, topSaving?.potentialSavingMinor ?: 0L)
        val months = if (suggestedTransfer > 0L) {
            ((remaining + suggestedTransfer - 1L) / suggestedTransfer).coerceAtLeast(1L)
        } else {
            null
        }

        return buildString {
            append("\"")
            append(goal.name)
            append("\" has ")
            append(formatMoney(remaining))
            append(" left.")
            if (months != null) {
                append(" If you move about ")
                append(formatMoney(suggestedTransfer))
                append(" per month from current surplus and identified savings opportunities, you can close that gap in roughly ")
                append(months)
                append(" months.")
            } else {
                append(" Current tracked cash flow is not positive, so reduce discretionary spend before setting a monthly transfer.")
            }
        }
    }

    private fun topSpendAnswer(summary: AnalyticsSummary): String {
        val topCategory = summary.topCategories.firstOrNull()
        val topMerchant = summary.topDiscretionaryMerchants.firstOrNull() ?: summary.topMerchants.firstOrNull()
        return buildString {
            if (topCategory != null) {
                append("Your top category is ")
                append(topCategory.category.displayName())
                append(" at ")
                append(formatMoney(topCategory.amountMinor))
                append(".")
            }
            if (topMerchant != null) {
                append("\n\nYour top merchant to review is ")
                append(topMerchant.merchantName)
                append(" at ")
                append(formatMoney(topMerchant.amountMinor))
                append(".")
            }
        }.ifBlank { "I do not have enough expense data yet to rank spending." }
    }

    private fun budgetHealthAnswer(summary: AnalyticsSummary, goals: List<SpendingGoal>): String {
        val topPressure = summary.categoryRecommendations.firstOrNull()
        val opportunity = summary.savingsOpportunities.firstOrNull()
        return buildString {
            append("Your budget health is ")
            append(summary.budgetHealth.status)
            append(" at ")
            append(summary.budgetHealth.score)
            append("/100. ")
            append(summary.budgetHealth.explanation)

            append("\n\nYour savings rate is ")
            append(summary.cashFlowAssessment.savingsRatePercent)
            append("%, fixed costs are ")
            append(summary.cashFlowAssessment.fixedCostRatioPercent)
            append("% of tracked income, and discretionary spend is ")
            append(summary.cashFlowAssessment.discretionaryRatioPercent)
            append("%.")

            if (topPressure != null) {
                append("\n\nMain pressure point: ")
                append(topPressure.category.displayName())
                append(" at ")
                append(formatMoney(topPressure.amountMinor))
                append(". ")
                append(topPressure.action)
            }

            if (opportunity != null) {
                append(" The cleanest optimization is ")
                append(opportunity.title)
                append(", which could free about ")
                append(formatMoney(opportunity.potentialSavingMinor))
                append(".")
            }

            goals.firstOrNull { it.targetAmountMinor > it.currentAmountMinor }?.let { goal ->
                append("\n\nKeep \"")
                append(goal.name)
                append("\" as the decision filter before large discretionary purchases.")
            }
        }
    }

    private fun planningAnswer(result: PlanningResult): String {
        return buildString {
            append(result.summary)
            append("\n\nVerdict: ")
            append(result.verdict.readableName())
            append(" (")
            append(result.confidence.readableName())
            append(" confidence).")

            if (result.facts.isNotEmpty()) {
                append("\n\nKey facts: ")
                append(result.facts.take(4).joinToString { "${it.label}: ${it.value}" })
                append(".")
            }

            if (result.risks.isNotEmpty()) {
                append("\n\nRisks: ")
                append(result.risks.take(2).joinToString { "${it.title} - ${it.detail}" })
            }

            if (result.missingInputs.isNotEmpty()) {
                append("\n\nI need: ")
                append(result.missingInputs.joinToString())
                append(".")
            }

            if (result.recommendations.isNotEmpty()) {
                append("\n\nNext step: ")
                val first = result.recommendations.first()
                append(first.title)
                append(" - ")
                append(first.detail)
            }
        }
    }

    private fun whatIfAnswer(question: String, summary: AnalyticsSummary, goals: List<SpendingGoal>): String {
        val amount = extractQuestionAmount(question)
        val opportunity = summary.savingsOpportunities.firstOrNull()
        val activeGoal = goals.firstOrNull { it.targetAmountMinor > it.currentAmountMinor }

        return buildString {
            if (amount != null) {
                append("For a ")
                append(formatMoney(amount))
                append(" decision, compare it against your current surplus of ")
                append(formatMoney(summary.netCashFlowMinor.coerceAtLeast(0L)))
                append(" and your savings rate of ")
                append(summary.cashFlowAssessment.savingsRatePercent)
                append("%.")

                if (summary.netCashFlowMinor >= amount) {
                    append(" It appears affordable from tracked cash flow, but I would still route it through a category cap.")
                } else {
                    append(" It is larger than the tracked surplus, so I would delay it or split it across months.")
                }
            } else {
                append("For a what-if decision, I would check three things: current surplus, whether the purchase is fixed/necessary/discretionary, and whether it slows a priority goal.")
            }

            if (activeGoal != null) {
                append("\n\nYour active goal \"")
                append(activeGoal.name)
                append("\" still needs ")
                append(formatMoney(activeGoal.targetAmountMinor - activeGoal.currentAmountMinor))
                append(", so any new purchase should have a clear tradeoff.")
            }

            if (opportunity != null) {
                append("\n\nTo make room, ")
                append(opportunity.title.lowercase())
                append(" could free around ")
                append(formatMoney(opportunity.potentialSavingMinor))
                append(".")
            }
        }
    }

    private fun casualReflectionAnswer(summary: AnalyticsSummary, goals: List<SpendingGoal>): String {
        val opportunity = summary.savingsOpportunities.firstOrNull()
        val topCategory = summary.topCategories.firstOrNull()
        val goal = goals.firstOrNull { it.targetAmountMinor > it.currentAmountMinor }

        return buildString {
            append("I hear you. Looking at the local data, this is not just a generic feeling: budget health is ")
            append(summary.budgetHealth.status)
            append(" at ")
            append(summary.budgetHealth.score)
            append("/100, with discretionary spend at ")
            append(summary.cashFlowAssessment.discretionaryRatioPercent)
            append("% of tracked income.")

            if (topCategory != null) {
                append("\n\nThe biggest visible pressure is ")
                append(topCategory.category.displayName())
                append(" at ")
                append(formatMoney(topCategory.amountMinor))
                append(".")
            }

            if (opportunity != null) {
                append(" I would not try to fix everything at once. Start with ")
                append(opportunity.title.lowercase())
                append(" and aim to free about ")
                append(formatMoney(opportunity.potentialSavingMinor))
                append(".")
            }

            if (goal != null) {
                append("\n\nThen send that freed money toward \"")
                append(goal.name)
                append("\" so the change feels useful, not restrictive.")
            }
        }
    }

    private fun advisorOverviewAnswer(summary: AnalyticsSummary, goals: List<SpendingGoal>): String {
        val opportunity = summary.savingsOpportunities.firstOrNull()
        return buildString {
            append("Here is the advisor view: ")
            append(summary.budgetHealth.status)
            append(" budget health, ")
            append(summary.cashFlowAssessment.savingsRatePercent)
            append("% savings rate, and ")
            append(formatMoney(summary.netCashFlowMinor))
            append(" tracked net cash flow.")

            append("\n\nThe important split is fixed costs at ")
            append(summary.cashFlowAssessment.fixedCostRatioPercent)
            append("% of income versus discretionary spend at ")
            append(summary.cashFlowAssessment.discretionaryRatioPercent)
            append("%. That tells us whether the next move should be renegotiating commitments or setting lifestyle caps.")

            if (opportunity != null) {
                append("\n\nMy first recommendation is ")
                append(opportunity.title.lowercase())
                append(": ")
                append(opportunity.rationale)
                append(" Potential saving is ")
                append(formatMoney(opportunity.potentialSavingMinor))
                append(".")
            }

            goals.firstOrNull { it.targetAmountMinor > it.currentAmountMinor }?.let { goal ->
                append("\n\nUse \"")
                append(goal.name)
                append("\" as the default destination for any freed cash.")
            }
        }
    }

    private fun groundedAdvice(
        summary: AnalyticsSummary,
        goals: List<SpendingGoal>,
    ): String {
        if (summary.transactionCount == 0) {
            return "I do not have enough local transaction data yet. Enable notification access and let SpendSense capture a few completed debits and credits before making a cut-back plan."
        }

        val cutCategory = summary.topCategories.firstOrNull { it.category.isDiscretionary() }
            ?: summary.topCategories.firstOrNull()
        val topMerchant = summary.topDiscretionaryMerchants.firstOrNull()
        val topOpportunity = summary.savingsOpportunities.firstOrNull()
        val savingsTargetMinor = cutCategory?.let { (it.amountMinor * 15L) / 100L } ?: 0L
        val cashFlowLabel = if (summary.netCashFlowMinor >= 0L) "surplus" else "gap"
        val activeGoal = goals
            .filter { it.targetAmountMinor > it.currentAmountMinor }
            .maxByOrNull { it.targetAmountMinor - it.currentAmountMinor }
        val surplusContributionMinor = if (summary.netCashFlowMinor > 0L) {
            (summary.netCashFlowMinor * 20L) / 100L
        } else {
            0L
        }
        val suggestedGoalContributionMinor = maxOf(savingsTargetMinor, surplusContributionMinor)
        val monthsToGoal = if (activeGoal != null && suggestedGoalContributionMinor > 0L) {
            val remaining = activeGoal.targetAmountMinor - activeGoal.currentAmountMinor
            ((remaining + suggestedGoalContributionMinor - 1L) / suggestedGoalContributionMinor).coerceAtLeast(1L)
        } else {
            null
        }

        return buildString {
            append("You have tracked ")
            append(formatMoney(summary.totalExpenseMinor))
            append(" in expenses and ")
            append(formatMoney(summary.totalIncomeMinor))
            append(" in income, leaving a ")
            append(cashFlowLabel)
            append(" of ")
            append(formatMoney(kotlin.math.abs(summary.netCashFlowMinor)))
            append(".")
            append(" Budget health is ")
            append(summary.budgetHealth.status)
            append(" at ")
            append(summary.budgetHealth.score)
            append("/100, with discretionary spend at ")
            append(summary.cashFlowAssessment.discretionaryRatioPercent)
            append("% of tracked income.")

            if (topOpportunity != null) {
                append("\n\nBest first move: ")
                append(topOpportunity.title)
                append(". ")
                append(topOpportunity.rationale)
                append(" A ")
                append(topOpportunity.suggestedCutPercent)
                append("% cap could free about ")
                append(formatMoney(topOpportunity.potentialSavingMinor))
                append(".")
            } else if (cutCategory != null && summary.totalExpenseMinor > 0L) {
                val share = ((cutCategory.amountMinor * 100L) / summary.totalExpenseMinor).toInt()
                append("\n\nStart with ")
                append(cutCategory.category.displayName())
                append(": it is ")
                append(formatMoney(cutCategory.amountMinor))
                append(", or ")
                append(share)
                append("% of tracked expenses.")

                if (savingsTargetMinor > 0L) {
                    append(" A practical first target is a 15% reduction, freeing about ")
                    append(formatMoney(savingsTargetMinor))
                    append(".")
                    if (activeGoal != null && monthsToGoal != null) {
                        append(" For \"")
                        append(activeGoal.name)
                        append("\", combine that cut with a planned monthly transfer of about ")
                        append(formatMoney(suggestedGoalContributionMinor))
                        append(" from your current surplus. That would cover the ")
                        append(formatMoney(activeGoal.targetAmountMinor - activeGoal.currentAmountMinor))
                        append(" remaining gap in roughly ")
                        append(monthsToGoal)
                        append(" months.")
                    }
                }
            }

            if (topMerchant != null) {
                append("\n\nWatch ")
                append(topMerchant.merchantName)
                append(" first, because it is your largest discretionary tracked merchant at ")
                append(formatMoney(topMerchant.amountMinor))
                append(".")
            }

            append("\n\nNext action: set a category cap, move the freed amount to the goal as soon as income lands, and review again after 5-10 more transactions.")
        }
    }

    private fun buildContext(
        summary: AnalyticsSummary,
        goals: List<SpendingGoal>,
        requiredAdvice: String,
        intent: AssistantIntent,
        planningResult: PlanningResult,
    ): String {
        return """
            Assistant intent:
            $intent

            Planning result:
            type=${planningResult.type}
            verdict=${planningResult.verdict}
            confidence=${planningResult.confidence}
            summary=${planningResult.summary}
            facts=${planningResult.facts.joinToString { "${it.label}: ${it.value}" }}
            risks=${planningResult.risks.joinToString { "${it.title}: ${it.detail}" }}
            recommendations=${planningResult.recommendations.joinToString { "${it.title}: ${it.detail}" }}
            assumptions=${planningResult.assumptions.joinToString()}
            missingInputs=${planningResult.missingInputs.joinToString()}

            Response style:
            ${intent.styleInstruction()}

            Required answer facts:
            $requiredAdvice

            Precomputed local analytics:
            totalExpense=${formatMoney(summary.totalExpenseMinor)}
            totalIncome=${formatMoney(summary.totalIncomeMinor)}
            netCashFlow=${formatMoney(summary.netCashFlowMinor)}
            transactionCount=${summary.transactionCount}
            topCategories=${summary.topCategories.joinToString { "${it.category.displayName()}=${formatMoney(it.amountMinor)}" }}
            topMerchants=${summary.topMerchants.joinToString { "${it.merchantName}=${formatMoney(it.amountMinor)}" }}
            topDiscretionaryMerchants=${summary.topDiscretionaryMerchants.take(3).joinToString { "${it.merchantName}=${formatMoney(it.amountMinor)}" }}
            spendingSegments=${summary.spendingSegments.joinToString { "${it.role}=${formatMoney(it.amountMinor)} (${it.sharePercent}%)" }}
            budgetHealth=${summary.budgetHealth.status}, score=${summary.budgetHealth.score}, reason=${summary.budgetHealth.explanation}
            cashFlowAssessment=expenseRatio:${summary.cashFlowAssessment.expenseRatioPercent}%, savingsRate:${summary.cashFlowAssessment.savingsRatePercent}%, fixedCosts:${summary.cashFlowAssessment.fixedCostRatioPercent}%, discretionary:${summary.cashFlowAssessment.discretionaryRatioPercent}%, investments:${summary.cashFlowAssessment.investmentRatePercent}%
            savingsOpportunities=${summary.savingsOpportunities.take(3).joinToString { "${it.title}: saves=${formatMoney(it.potentialSavingMinor)}, rationale=${it.rationale}" }}
            categoryRecommendations=${summary.categoryRecommendations.take(5).joinToString { "${it.category.displayName()}: share=${it.sharePercent}%, action=${it.action}, cap=${it.suggestedCapMinor?.let(::formatMoney) ?: "none"}" }}
            goals=${goals.joinToString { "${it.name}: ${formatMoney(it.currentAmountMinor)} saved of ${formatMoney(it.targetAmountMinor)}" }}
        """.trimIndent()
    }

    private fun isAcceptableModelAnswer(
        answer: String,
        requiredAnswer: String,
        summary: AnalyticsSummary,
        intent: AssistantIntent,
        planningResult: PlanningResult,
    ): Boolean {
        val normalized = answer.lowercase()
        val blockedPhrases = listOf(
            "maximize spending",
            "maximize your spending",
            "use the highest amount",
            "highest amount",
            "provided data",
            "income goals",
            "largest tracked income",
            "largest tracked merchant at",
            "free you from",
            "cutting back on this will help",
            "adjust your spending accordingly",
        )
        if (blockedPhrases.any { it in normalized }) return false

        val requiredNormalized = requiredAnswer.lowercase()
        val requiredAmountMentioned = moneyTokens(requiredNormalized).any { it in normalized }
        val requiredCategoryMentioned = summary.categorySpends.any {
            it.category.displayName().lowercase() in requiredNormalized &&
                it.category.displayName().lowercase() in normalized
        }
        val requiredMerchantMentioned = summary.merchantSpends.any {
            it.merchantName.lowercase() in requiredNormalized &&
                it.merchantName.lowercase() in normalized
        }

        return when (intent) {
            AssistantIntent.CATEGORY_TOTAL,
            AssistantIntent.MERCHANT_TOTAL,
            AssistantIntent.CASH_FLOW,
            AssistantIntent.GOAL_PLAN,
            AssistantIntent.TOP_SPEND -> summary.transactionCount == 0 ||
                requiredAmountMentioned ||
                requiredCategoryMentioned ||
                requiredMerchantMentioned
            AssistantIntent.CUTBACK_PLAN -> requiredAmountMentioned ||
                requiredCategoryMentioned ||
                requiredMerchantMentioned ||
                normalized.contains("budget health")
            AssistantIntent.BUDGET_HEALTH -> normalized.contains(summary.budgetHealth.status.lowercase()) ||
                normalized.contains("${summary.budgetHealth.score}") ||
                normalized.contains("savings rate") ||
                normalized.contains("discretionary")
            AssistantIntent.WHAT_IF,
            AssistantIntent.LOAN_PLANNING,
            AssistantIntent.EMERGENCY_FUND,
            AssistantIntent.RENT_BURDEN,
            AssistantIntent.TAX_PLANNING -> containsPlanningFact(normalized, planningResult) ||
                containsAnyGroundedAdvisorFact(normalized, summary)
            AssistantIntent.CASUAL_REFLECTION,
            AssistantIntent.GENERAL_FINANCE_COACHING -> containsAnyGroundedAdvisorFact(normalized, summary)
        }
    }

    private fun containsPlanningFact(answer: String, result: PlanningResult): Boolean {
        if (result.missingInputs.isNotEmpty()) {
            return result.missingInputs.any { it.lowercase() in answer } ||
                answer.contains("missing") ||
                answer.contains("need")
        }

        return answer.contains(result.verdict.readableName().lowercase()) ||
            result.facts.any { fact ->
                fact.value.lowercase() in answer ||
                    fact.label.lowercase() in answer
            } ||
            result.recommendations.any { recommendation ->
                recommendation.title.lowercase() in answer
            } ||
            result.risks.any { risk -> risk.title.lowercase() in answer }
    }

    private fun containsAnyGroundedAdvisorFact(answer: String, summary: AnalyticsSummary): Boolean {
        return answer.contains(summary.budgetHealth.status.lowercase()) ||
            answer.contains("${summary.budgetHealth.score}") ||
            answer.contains("savings rate") ||
            answer.contains("discretionary") ||
            summary.topCategories.any { it.category.displayName().lowercase() in answer } ||
            summary.savingsOpportunities.any { opportunity ->
                opportunity.title.lowercase() in answer ||
                    opportunity.category?.displayName()?.lowercase()?.let { it in answer } == true ||
                    opportunity.merchantName?.lowercase()?.let { it in answer } == true
            } ||
            moneyTokens(answer).isNotEmpty()
    }

    private fun moneyTokens(text: String): List<String> {
        return Regex("""₹[0-9,]+(?:\.[0-9]{1,2})?""").findAll(text).map { it.value }.toList()
    }

    private fun formatMoney(amountMinor: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            currency = Currency.getInstance("INR")
            maximumFractionDigits = if (amountMinor % 100L == 0L) 0 else 2
        }
        return formatter.format(amountMinor / 100.0)
    }

    private fun TransactionCategory.displayName(): String {
        return name.lowercase().replaceFirstChar { it.titlecase() }
    }

    private fun String.hasSpendQuestionTerms(): Boolean {
        return contains("spend") ||
            contains("spent") ||
            contains("how much") ||
            contains("where") ||
            contains("cost")
    }

    private fun String.hasCutbackQuestionTerms(): Boolean {
        return contains("cut") ||
            contains("cut back") ||
            contains("reduce") ||
            contains("save money") ||
            contains("lower") ||
            contains("trim")
    }

    private fun classifyIntent(question: String, summary: AnalyticsSummary): AssistantIntent {
        val normalized = question.lowercase()
        val requestedCategory = TransactionCategory.entries.any { category ->
            val label = category.displayName().lowercase()
            normalized.contains(label) || normalized.contains(category.name.lowercase())
        }
        val requestedMerchant = summary.merchantSpends.any { merchant ->
            normalized.contains(merchant.merchantName.lowercase())
        }

        return when {
            normalized.contains("tax") ||
                normalized.contains("deduction") ||
                normalized.contains("80c") ||
                normalized.contains("itr") -> AssistantIntent.TAX_PLANNING
            normalized.contains("loan") ||
                normalized.contains("emi") ||
                normalized.contains("borrow") ||
                normalized.contains("interest rate") -> AssistantIntent.LOAN_PLANNING
            normalized.contains("emergency fund") ||
                normalized.contains("runway") ||
                normalized.contains("rainy day") -> AssistantIntent.EMERGENCY_FUND
            normalized.contains("rent") &&
                (normalized.contains("too high") ||
                    normalized.contains("burden") ||
                    normalized.contains("okay") ||
                    normalized.contains("afford")) -> AssistantIntent.RENT_BURDEN
            requestedCategory && normalized.hasSpendQuestionTerms() -> AssistantIntent.CATEGORY_TOTAL
            requestedMerchant && normalized.hasSpendQuestionTerms() -> AssistantIntent.MERCHANT_TOTAL
            normalized.hasCutbackQuestionTerms() -> AssistantIntent.CUTBACK_PLAN
            normalized.contains("can i buy") ||
                normalized.contains("should i buy") ||
                normalized.contains("what if") ||
                normalized.contains("can i spend") ||
                normalized.contains("is it okay to spend") ||
                extractQuestionAmount(question) != null && (
                    normalized.contains("buy") ||
                        normalized.contains("purchase") ||
                        normalized.contains("spend")
                    ) -> AssistantIntent.WHAT_IF
            normalized.contains("cash flow") ||
                normalized.contains("surplus") ||
                normalized.contains("income") ||
                normalized.contains("salary") -> AssistantIntent.CASH_FLOW
            normalized.contains("goal") ||
                normalized.contains("afford") ||
                normalized.contains("hit") ||
                normalized.contains("save for") -> AssistantIntent.GOAL_PLAN
            normalized.contains("top") ||
                normalized.contains("most") ||
                normalized.contains("biggest") ||
                normalized.contains("where is my money going") -> AssistantIntent.TOP_SPEND
            normalized.contains("budget health") ||
                normalized.contains("doing okay") ||
                normalized.contains("am i okay") ||
                normalized.contains("score") ||
                normalized.contains("healthy") -> AssistantIntent.BUDGET_HEALTH
            normalized.contains("i feel") ||
                normalized.contains("worried") ||
                normalized.contains("stress") ||
                normalized.contains("anxious") ||
                normalized.contains("too much") ||
                normalized.contains("confused") ||
                normalized.contains("help me understand") -> AssistantIntent.CASUAL_REFLECTION
            else -> AssistantIntent.GENERAL_FINANCE_COACHING
        }
    }

    private fun extractQuestionAmount(question: String): Long? {
        val match = Regex("""(?:₹|rs\.?|inr)?\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
            .find(question)
            ?: return null
        val major = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null
        return (major * 100).toLong()
    }

    private fun AssistantIntent.styleInstruction(): String {
        return when (this) {
            AssistantIntent.CATEGORY_TOTAL,
            AssistantIntent.MERCHANT_TOTAL -> "Answer directly with the exact amount first, then give one useful interpretation."
            AssistantIntent.CUTBACK_PLAN -> "Act like a practical financial advisor. Prioritize one lever, explain why, and give a next action."
            AssistantIntent.CASH_FLOW -> "Explain income, expenses, surplus or gap, then connect it to savings behavior."
            AssistantIntent.GOAL_PLAN -> "Connect the goal gap to monthly cash flow and tradeoffs. Be specific."
            AssistantIntent.TOP_SPEND -> "Rank the main pressure points and explain what they mean."
            AssistantIntent.BUDGET_HEALTH -> "Give a balanced health assessment, not just a cutback recommendation."
            AssistantIntent.WHAT_IF -> "Evaluate affordability, tradeoff, and timing. Do not moralize."
            AssistantIntent.LOAN_PLANNING -> "Use calculated EMI, debt burden, post-EMI surplus, and missing inputs. Do not guess loan details."
            AssistantIntent.EMERGENCY_FUND -> "Explain core monthly expenses, 3-month minimum, 6-month strong target, and current known gap."
            AssistantIntent.RENT_BURDEN -> "Compare rent to income and explain fixed-cost pressure clearly."
            AssistantIntent.TAX_PLANNING -> "Do not provide tax law claims. Ask for jurisdiction, regime, income, deductions, and filing status."
            AssistantIntent.CASUAL_REFLECTION -> "Respond conversationally and empathetically, then ground the feeling in one or two financial facts."
            AssistantIntent.GENERAL_FINANCE_COACHING -> "Give an advisor overview with diagnosis, key lever, and next step."
        }
    }

    private fun PlanningVerdict.readableName(): String {
        return name.lowercase().replace("_", " ").replaceFirstChar { it.titlecase() }
    }

    private fun PlanningConfidence.readableName(): String {
        return name.lowercase().replaceFirstChar { it.titlecase() }
    }

}
