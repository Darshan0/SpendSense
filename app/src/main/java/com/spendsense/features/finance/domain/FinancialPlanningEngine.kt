package com.spendsense.features.finance.domain

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale
import kotlin.math.abs
import kotlin.math.pow

class FinancialPlanningEngine {
    fun plan(
        question: String,
        intent: AssistantIntent,
        summary: AnalyticsSummary,
        goals: List<SpendingGoal>,
    ): PlanningResult {
        return when (intent) {
            AssistantIntent.WHAT_IF -> purchaseAffordability(question, summary, goals)
            AssistantIntent.LOAN_PLANNING -> loanAffordability(question, summary, goals)
            AssistantIntent.EMERGENCY_FUND -> emergencyFund(summary, goals)
            AssistantIntent.RENT_BURDEN -> rentBurden(summary)
            AssistantIntent.TAX_PLANNING -> taxReadiness()
            AssistantIntent.GOAL_PLAN -> goalRunway(summary, goals)
            else -> generalAdvisor(summary, goals)
        }
    }

    fun calculateEmi(principalMinor: Long, annualRatePercent: Double, tenureMonths: Int): Long {
        if (principalMinor <= 0L || annualRatePercent < 0.0 || tenureMonths <= 0) return 0L
        if (annualRatePercent == 0.0) return principalMinor / tenureMonths

        val monthlyRate = annualRatePercent / 12.0 / 100.0
        val multiplier = (1.0 + monthlyRate).pow(tenureMonths)
        val emiMajor = (principalMinor / 100.0) * monthlyRate * multiplier / (multiplier - 1.0)
        return (emiMajor * 100.0).toLong()
    }

    private fun purchaseAffordability(
        question: String,
        summary: AnalyticsSummary,
        goals: List<SpendingGoal>,
    ): PlanningResult {
        val amount = extractAmountMinor(question)
        if (amount == null) {
            return needsInfo(
                type = PlanningType.PURCHASE_AFFORDABILITY,
                summary = "I need the purchase amount before I can judge affordability.",
                missingInputs = listOf("Purchase amount"),
            )
        }

        val surplus = summary.netCashFlowMinor.coerceAtLeast(0L)
        val savingsRate = summary.cashFlowAssessment.savingsRatePercent
        val postPurchaseSurplus = surplus - amount
        val postPurchaseSavingsRate = percent(postPurchaseSurplus.coerceAtLeast(0L), summary.totalIncomeMinor)
        val activeGoal = goals.firstOrNull { it.targetAmountMinor > it.currentAmountMinor }
        val opportunity = summary.savingsOpportunities.firstOrNull()

        val verdict = when {
            summary.totalIncomeMinor <= 0L -> PlanningVerdict.NEEDS_MORE_INFO
            amount <= surplus / 4L && savingsRate >= 20 -> PlanningVerdict.AFFORDABLE
            amount <= surplus && savingsRate >= 10 -> PlanningVerdict.CAUTION
            else -> PlanningVerdict.DELAY
        }
        val confidence = if (summary.totalIncomeMinor <= 0L) PlanningConfidence.LOW else PlanningConfidence.HIGH

        return PlanningResult(
            type = PlanningType.PURCHASE_AFFORDABILITY,
            verdict = verdict,
            confidence = confidence,
            summary = when (verdict) {
                PlanningVerdict.AFFORDABLE -> "This purchase looks affordable from tracked monthly cash flow."
                PlanningVerdict.CAUTION -> "This purchase is possible, but it uses a visible part of your current surplus."
                PlanningVerdict.DELAY -> "I would delay or split this purchase because it pressures your surplus or goal progress."
                PlanningVerdict.NEEDS_MORE_INFO -> "I need income data before judging affordability confidently."
                PlanningVerdict.NOT_SUPPORTED_YET -> "Purchase planning is not available."
            },
            facts = listOf(
                PlanningFact("Purchase amount", formatMoney(amount)),
                PlanningFact("Current surplus", formatMoney(surplus)),
                PlanningFact("Post-purchase surplus", formatMoney(postPurchaseSurplus)),
                PlanningFact("Current savings rate", "$savingsRate%"),
                PlanningFact("Post-purchase savings rate", "$postPurchaseSavingsRate%"),
            ) + listOfNotNull(
                activeGoal?.let {
                    PlanningFact("Priority goal gap", "${it.name}: ${formatMoney(it.targetAmountMinor - it.currentAmountMinor)}")
                },
            ),
            risks = listOfNotNull(
                if (postPurchaseSavingsRate < 10 && summary.totalIncomeMinor > 0L) {
                    PlanningRisk(
                        title = "Savings rate pressure",
                        severity = InsightSeverity.WARNING,
                        detail = "After this purchase, savings rate would fall to $postPurchaseSavingsRate%.",
                    )
                } else {
                    null
                },
                activeGoal?.let {
                    PlanningRisk(
                        title = "Goal tradeoff",
                        severity = InsightSeverity.INFO,
                        detail = "This should be compared against ${it.name} before spending.",
                    )
                },
            ),
            recommendations = listOfNotNull(
                ActionStep(
                    title = "Use a purchase rule",
                    detail = "Buy now only if the post-purchase savings rate stays above 10% and core bills are already covered.",
                ),
                opportunity?.let {
                    ActionStep(
                        title = it.title,
                        detail = "Free about ${formatMoney(it.potentialSavingMinor)} first, then decide.",
                    )
                },
            ),
            assumptions = listOf("Tracked income and expenses represent the current month."),
            missingInputs = if (summary.totalIncomeMinor <= 0L) listOf("Monthly income") else emptyList(),
        )
    }

    private fun loanAffordability(
        question: String,
        summary: AnalyticsSummary,
        goals: List<SpendingGoal>,
    ): PlanningResult {
        val principal = extractAmountMinor(question)
        val rate = extractRate(question)
        val tenureMonths = extractTenureMonths(question)
        val missing = buildList {
            if (principal == null) add("Loan principal")
            if (rate == null) add("Annual interest rate")
            if (tenureMonths == null) add("Loan tenure")
            if (summary.totalIncomeMinor <= 0L) add("Monthly income")
        }

        if (missing.isNotEmpty()) {
            return needsInfo(
                type = PlanningType.LOAN_AFFORDABILITY,
                summary = "I need the loan amount, annual interest rate, tenure, and monthly income before I can calculate affordability.",
                missingInputs = missing,
            )
        }

        val emi = calculateEmi(principal!!, rate!!, tenureMonths!!)
        val emiBurden = percent(emi, summary.totalIncomeMinor)
        val postEmiSurplus = summary.netCashFlowMinor - emi
        val postEmiSavingsRate = percent(postEmiSurplus.coerceAtLeast(0L), summary.totalIncomeMinor)
        val activeGoal = goals.firstOrNull { it.targetAmountMinor > it.currentAmountMinor }

        val verdict = when {
            emiBurden <= 20 && postEmiSavingsRate >= 20 -> PlanningVerdict.AFFORDABLE
            emiBurden <= 35 && postEmiSavingsRate >= 10 -> PlanningVerdict.CAUTION
            else -> PlanningVerdict.DELAY
        }

        return PlanningResult(
            type = PlanningType.LOAN_AFFORDABILITY,
            verdict = verdict,
            confidence = PlanningConfidence.HIGH,
            summary = when (verdict) {
                PlanningVerdict.AFFORDABLE -> "The EMI looks affordable against current tracked income and surplus."
                PlanningVerdict.CAUTION -> "The EMI may be manageable, but it reduces flexibility."
                PlanningVerdict.DELAY -> "I would not take this loan yet because EMI pressure is too high for current cash flow."
                PlanningVerdict.NEEDS_MORE_INFO -> "More loan inputs are needed."
                PlanningVerdict.NOT_SUPPORTED_YET -> "Loan planning is not available."
            },
            facts = listOf(
                PlanningFact("Loan principal", formatMoney(principal)),
                PlanningFact("Annual interest rate", "${trimRate(rate)}%"),
                PlanningFact("Tenure", "$tenureMonths months"),
                PlanningFact("Estimated EMI", formatMoney(emi)),
                PlanningFact("EMI burden", "$emiBurden% of income"),
                PlanningFact("Post-EMI surplus", formatMoney(postEmiSurplus)),
                PlanningFact("Post-EMI savings rate", "$postEmiSavingsRate%"),
            ),
            risks = listOfNotNull(
                if (emiBurden > 35) {
                    PlanningRisk(
                        title = "High EMI burden",
                        severity = InsightSeverity.WARNING,
                        detail = "EMI would consume $emiBurden% of tracked income.",
                    )
                } else {
                    null
                },
                if (postEmiSavingsRate < 10) {
                    PlanningRisk(
                        title = "Low post-EMI savings",
                        severity = InsightSeverity.WARNING,
                        detail = "Post-EMI savings rate would be $postEmiSavingsRate%.",
                    )
                } else {
                    null
                },
                activeGoal?.let {
                    PlanningRisk(
                        title = "Goal slowdown",
                        severity = InsightSeverity.INFO,
                        detail = "This EMI competes with ${it.name}, which still needs ${formatMoney(it.targetAmountMinor - it.currentAmountMinor)}.",
                    )
                },
            ),
            recommendations = listOf(
                ActionStep(
                    title = "Stress test the EMI",
                    detail = "Keep EMI burden below 30% and post-EMI savings rate above 10% before committing.",
                ),
                ActionStep(
                    title = "Reduce loan size if needed",
                    detail = "A smaller principal or longer saving period will protect emergency liquidity.",
                ),
            ),
            assumptions = listOf("No other existing EMIs are currently modeled."),
        )
    }

    private fun emergencyFund(summary: AnalyticsSummary, goals: List<SpendingGoal>): PlanningResult {
        val coreExpense = summary.spendingSegments
            .filter { it.role == ExpenseRole.FIXED_NEED || it.role == ExpenseRole.VARIABLE_NEED }
            .sumOf { it.amountMinor }
            .ifZero { summary.totalExpenseMinor }
        val minimumTarget = coreExpense * 3L
        val strongTarget = coreExpense * 6L
        val emergencyGoal = goals.firstOrNull { it.name.contains("emergency", ignoreCase = true) }
        val currentKnown = emergencyGoal?.currentAmountMinor
        val currentMonths = currentKnown?.let { if (coreExpense <= 0L) 0 else it / coreExpense }

        return PlanningResult(
            type = PlanningType.EMERGENCY_FUND,
            verdict = when {
                currentKnown == null -> PlanningVerdict.NEEDS_MORE_INFO
                currentKnown >= strongTarget -> PlanningVerdict.AFFORDABLE
                currentKnown >= minimumTarget -> PlanningVerdict.CAUTION
                else -> PlanningVerdict.DELAY
            },
            confidence = if (currentKnown == null) PlanningConfidence.MEDIUM else PlanningConfidence.HIGH,
            summary = if (currentKnown == null) {
                "Your estimated emergency fund target is available, but I need current liquid emergency savings to measure the gap."
            } else {
                "Your emergency fund covers about $currentMonths months of core expenses."
            },
            facts = listOf(
                PlanningFact("Core monthly expenses", formatMoney(coreExpense)),
                PlanningFact("Minimum target", formatMoney(minimumTarget)),
                PlanningFact("Strong target", formatMoney(strongTarget)),
            ) + listOfNotNull(
                currentKnown?.let { PlanningFact("Known emergency savings", formatMoney(it)) },
            ),
            risks = listOfNotNull(
                if (currentKnown != null && currentKnown < minimumTarget) {
                    PlanningRisk(
                        title = "Emergency fund gap",
                        severity = InsightSeverity.WARNING,
                        detail = "Known emergency savings are below the 3-month minimum target.",
                    )
                } else {
                    null
                },
            ),
            recommendations = listOf(
                ActionStep(
                    title = "Build the 3-month floor first",
                    detail = "Treat ${formatMoney(minimumTarget)} as the first safety target before aggressive discretionary goals.",
                ),
            ),
            assumptions = listOf("Core expenses are estimated from fixed and variable-need categories."),
            missingInputs = if (currentKnown == null) listOf("Current liquid emergency savings") else emptyList(),
        )
    }

    private fun rentBurden(summary: AnalyticsSummary): PlanningResult {
        val rent = summary.categorySpends.firstOrNull { it.category == TransactionCategory.RENT }?.amountMinor
        if (rent == null || summary.totalIncomeMinor <= 0L) {
            return needsInfo(
                type = PlanningType.RENT_BURDEN,
                summary = "I need tracked rent and monthly income to judge rent burden.",
                missingInputs = buildList {
                    if (rent == null) add("Monthly rent")
                    if (summary.totalIncomeMinor <= 0L) add("Monthly income")
                },
            )
        }

        val rentRatio = percent(rent, summary.totalIncomeMinor)
        val verdict = when {
            rentRatio <= 30 -> PlanningVerdict.AFFORDABLE
            rentRatio <= 40 -> PlanningVerdict.CAUTION
            else -> PlanningVerdict.DELAY
        }

        return PlanningResult(
            type = PlanningType.RENT_BURDEN,
            verdict = verdict,
            confidence = PlanningConfidence.HIGH,
            summary = when (verdict) {
                PlanningVerdict.AFFORDABLE -> "Rent looks healthy relative to tracked income."
                PlanningVerdict.CAUTION -> "Rent is manageable, but it is high enough to watch."
                PlanningVerdict.DELAY -> "Rent is a heavy burden relative to tracked income."
                PlanningVerdict.NEEDS_MORE_INFO -> "More rent inputs are needed."
                PlanningVerdict.NOT_SUPPORTED_YET -> "Rent planning is not available."
            },
            facts = listOf(
                PlanningFact("Rent", formatMoney(rent)),
                PlanningFact("Monthly income", formatMoney(summary.totalIncomeMinor)),
                PlanningFact("Rent burden", "$rentRatio% of income"),
            ),
            risks = listOfNotNull(
                if (rentRatio > 40) {
                    PlanningRisk(
                        title = "High fixed-cost pressure",
                        severity = InsightSeverity.WARNING,
                        detail = "Rent above 40% reduces flexibility for savings and goals.",
                    )
                } else {
                    null
                },
            ),
            recommendations = listOf(
                ActionStep(
                    title = "Use rent as a fixed-cost anchor",
                    detail = "If rent stays above 35%, avoid adding new EMI commitments without increasing income or cutting discretionary spend.",
                ),
            ),
        )
    }

    private fun goalRunway(summary: AnalyticsSummary, goals: List<SpendingGoal>): PlanningResult {
        val goal = goals
            .filter { it.targetAmountMinor > it.currentAmountMinor }
            .maxByOrNull { it.targetAmountMinor - it.currentAmountMinor }
        if (goal == null) {
            return needsInfo(
                type = PlanningType.GOAL_RUNWAY,
                summary = "I need an active goal with target and current savings to calculate runway.",
                missingInputs = listOf("Active financial goal"),
            )
        }

        val gap = goal.targetAmountMinor - goal.currentAmountMinor
        val opportunity = summary.savingsOpportunities.firstOrNull()
        val monthlyCapacity = maxOf(summary.netCashFlowMinor.coerceAtLeast(0L) / 5L, opportunity?.potentialSavingMinor ?: 0L)
        val months = if (monthlyCapacity > 0L) ((gap + monthlyCapacity - 1L) / monthlyCapacity).coerceAtLeast(1L) else null

        return PlanningResult(
            type = PlanningType.GOAL_RUNWAY,
            verdict = if (months == null) PlanningVerdict.NEEDS_MORE_INFO else PlanningVerdict.CAUTION,
            confidence = if (months == null) PlanningConfidence.LOW else PlanningConfidence.MEDIUM,
            summary = if (months == null) {
                "Current tracked cash flow does not show clear monthly capacity for this goal yet."
            } else {
                "${goal.name} can be planned over roughly $months months using current surplus or savings opportunities."
            },
            facts = listOf(
                PlanningFact("Goal", goal.name),
                PlanningFact("Goal gap", formatMoney(gap)),
                PlanningFact("Estimated monthly capacity", formatMoney(monthlyCapacity)),
            ) + listOfNotNull(months?.let { PlanningFact("Estimated runway", "$it months") }),
            risks = listOf(
                PlanningRisk(
                    title = "Capacity may vary",
                    severity = InsightSeverity.INFO,
                    detail = "Runway uses tracked data and should be revisited after more transactions.",
                ),
            ),
            recommendations = listOfNotNull(
                opportunity?.let {
                    ActionStep(
                        title = it.title,
                        detail = "Redirect ${formatMoney(it.potentialSavingMinor)} toward ${goal.name}.",
                    )
                },
                ActionStep(
                    title = "Automate the transfer",
                    detail = "Move the planned amount when income lands so goals are funded before discretionary spend.",
                ),
            ),
        )
    }

    private fun taxReadiness(): PlanningResult {
        return PlanningResult(
            type = PlanningType.TAX_READINESS,
            verdict = PlanningVerdict.NOT_SUPPORTED_YET,
            confidence = PlanningConfidence.LOW,
            summary = "Tax planning needs jurisdiction-specific rules and is not implemented in this local planner yet.",
            facts = emptyList(),
            risks = listOf(
                PlanningRisk(
                    title = "Tax law depends on jurisdiction",
                    severity = InsightSeverity.WARNING,
                    detail = "I should not invent tax-saving advice without country, regime, income, deductions, and filing status.",
                ),
            ),
            recommendations = listOf(
                ActionStep(
                    title = "Collect tax inputs first",
                    detail = "Ask for country/state, tax regime, gross income, deductions, investments, and filing status.",
                ),
            ),
            missingInputs = listOf(
                "Country or tax jurisdiction",
                "Tax regime",
                "Gross annual income",
                "Deductions and exemptions",
                "Filing status",
            ),
        )
    }

    private fun generalAdvisor(summary: AnalyticsSummary, goals: List<SpendingGoal>): PlanningResult {
        val opportunity = summary.savingsOpportunities.firstOrNull()
        val goal = goals.firstOrNull { it.targetAmountMinor > it.currentAmountMinor }
        return PlanningResult(
            type = PlanningType.GENERAL_ADVISOR,
            verdict = when {
                summary.budgetHealth.score >= 75 -> PlanningVerdict.AFFORDABLE
                summary.budgetHealth.score >= 50 -> PlanningVerdict.CAUTION
                else -> PlanningVerdict.DELAY
            },
            confidence = if (summary.transactionCount >= 10) PlanningConfidence.HIGH else PlanningConfidence.MEDIUM,
            summary = "Budget health is ${summary.budgetHealth.status}, with ${summary.cashFlowAssessment.savingsRatePercent}% savings rate and ${summary.cashFlowAssessment.discretionaryRatioPercent}% discretionary spend.",
            facts = listOf(
                PlanningFact("Budget score", "${summary.budgetHealth.score}/100"),
                PlanningFact("Savings rate", "${summary.cashFlowAssessment.savingsRatePercent}%"),
                PlanningFact("Discretionary spend", "${summary.cashFlowAssessment.discretionaryRatioPercent}% of income"),
                PlanningFact("Net cash flow", formatMoney(summary.netCashFlowMinor)),
            ),
            risks = listOfNotNull(
                if (summary.cashFlowAssessment.discretionaryRatioPercent > 35) {
                    PlanningRisk(
                        title = "High discretionary pressure",
                        severity = InsightSeverity.WARNING,
                        detail = "Lifestyle categories are high enough to slow goals.",
                    )
                } else {
                    null
                },
            ),
            recommendations = listOfNotNull(
                opportunity?.let {
                    ActionStep(
                        title = it.title,
                        detail = "Potential saving: ${formatMoney(it.potentialSavingMinor)}.",
                    )
                },
                goal?.let {
                    ActionStep(
                        title = "Fund ${it.name}",
                        detail = "Use freed cash as the default destination.",
                    )
                },
            ),
        )
    }

    private fun needsInfo(
        type: PlanningType,
        summary: String,
        missingInputs: List<String>,
    ): PlanningResult {
        return PlanningResult(
            type = type,
            verdict = PlanningVerdict.NEEDS_MORE_INFO,
            confidence = PlanningConfidence.LOW,
            summary = summary,
            missingInputs = missingInputs,
            recommendations = listOf(
                ActionStep(
                    title = "Add missing details",
                    detail = missingInputs.joinToString(),
                ),
            ),
        )
    }

    private fun extractAmountMinor(question: String): Long? {
        val normalized = question.lowercase()
        val match = Regex("""(?:₹|rs\.?|inr)?\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)\s*(lakh|lakhs|lac|lacs|k)?""")
            .find(normalized)
            ?: return null
        val value = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: return null
        val multiplier = when (match.groupValues.getOrNull(2).orEmpty()) {
            "lakh", "lakhs", "lac", "lacs" -> 100_000.0
            "k" -> 1_000.0
            else -> 1.0
        }
        return (value * multiplier * 100.0).toLong()
    }

    private fun extractRate(question: String): Double? {
        val normalized = question.lowercase()
        return Regex("""([0-9]+(?:\.[0-9]+)?)\s*%""")
            .find(normalized)
            ?.groupValues
            ?.getOrNull(1)
            ?.toDoubleOrNull()
    }

    private fun extractTenureMonths(question: String): Int? {
        val normalized = question.lowercase()
        val match = Regex("""([0-9]+)\s*(years|year|yrs|yr|months|month|mo)""")
            .find(normalized)
            ?: return null
        val amount = match.groupValues[1].toIntOrNull() ?: return null
        return when (match.groupValues[2]) {
            "years", "year", "yrs", "yr" -> amount * 12
            else -> amount
        }
    }

    private fun percent(numerator: Long, denominator: Long): Int {
        return if (denominator <= 0L) 0 else ((numerator * 100L) / denominator).toInt()
    }

    private fun formatMoney(amountMinor: Long): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            currency = Currency.getInstance("INR")
            maximumFractionDigits = if (abs(amountMinor) % 100L == 0L) 0 else 2
        }
        return formatter.format(amountMinor / 100.0)
    }

    private fun trimRate(rate: Double): String {
        return if (rate % 1.0 == 0.0) rate.toInt().toString() else rate.toString()
    }

    private fun Long.ifZero(fallback: () -> Long): Long {
        return if (this == 0L) fallback() else this
    }
}
