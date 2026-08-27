package com.spendsense.features.finance.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialPlanningEngineTest {
    private val engine = FinancialPlanningEngine()

    @Test
    fun calculatesReducingBalanceEmi() {
        val emi = engine.calculateEmi(
            principalMinor = 1_000_000_00,
            annualRatePercent = 10.0,
            tenureMonths = 60,
        )

        assertTrue(emi in 21_200_00..21_300_00)
    }

    @Test
    fun purchaseAffordabilityUsesSurplusAndSavingsRate() {
        val result = engine.plan(
            question = "Can I buy a phone for ₹50,000?",
            intent = AssistantIntent.WHAT_IF,
            summary = planningSummary(),
            goals = goals(),
        )

        assertEquals(PlanningType.PURCHASE_AFFORDABILITY, result.type)
        assertEquals(PlanningVerdict.DELAY, result.verdict)
        assertTrue(result.facts.any { it.label == "Purchase amount" && it.value.contains("50,000") })
        assertTrue(result.risks.any { it.title == "Goal tradeoff" })
    }

    @Test
    fun loanPlanningCalculatesEmiWhenInputsExist() {
        val result = engine.plan(
            question = "Can I afford a ₹10 lakh loan at 10% for 5 years?",
            intent = AssistantIntent.LOAN_PLANNING,
            summary = planningSummary(),
            goals = goals(),
        )

        assertEquals(PlanningType.LOAN_AFFORDABILITY, result.type)
        assertEquals(PlanningVerdict.DELAY, result.verdict)
        assertTrue(result.facts.any { it.label == "Estimated EMI" && it.value.contains("21,") })
        assertTrue(result.facts.any { it.label == "EMI burden" })
    }

    @Test
    fun loanPlanningReturnsMissingInputsInsteadOfGuessing() {
        val result = engine.plan(
            question = "Can I afford a loan?",
            intent = AssistantIntent.LOAN_PLANNING,
            summary = planningSummary(),
            goals = goals(),
        )

        assertEquals(PlanningVerdict.NEEDS_MORE_INFO, result.verdict)
        assertTrue(result.missingInputs.contains("Loan principal"))
        assertTrue(result.missingInputs.contains("Annual interest rate"))
        assertTrue(result.missingInputs.contains("Loan tenure"))
    }

    @Test
    fun rentBurdenUsesRentToIncomeRatio() {
        val result = engine.plan(
            question = "Is my rent too high?",
            intent = AssistantIntent.RENT_BURDEN,
            summary = planningSummary(),
            goals = goals(),
        )

        assertEquals(PlanningType.RENT_BURDEN, result.type)
        assertEquals(PlanningVerdict.AFFORDABLE, result.verdict)
        assertTrue(result.facts.any { it.label == "Rent burden" && it.value == "30% of income" })
    }

    @Test
    fun emergencyFundUsesCoreExpensesAndKnownEmergencyGoal() {
        val result = engine.plan(
            question = "How much emergency fund do I need?",
            intent = AssistantIntent.EMERGENCY_FUND,
            summary = planningSummary(),
            goals = goals(),
        )

        assertEquals(PlanningType.EMERGENCY_FUND, result.type)
        assertTrue(result.facts.any { it.label == "Minimum target" && it.value.contains("210") })
        assertTrue(result.facts.any { it.label == "Strong target" && it.value.contains("420") })
    }

    @Test
    fun taxPlanningDoesNotFabricateTaxLaw() {
        val result = engine.plan(
            question = "How can I save tax?",
            intent = AssistantIntent.TAX_PLANNING,
            summary = planningSummary(),
            goals = goals(),
        )

        assertEquals(PlanningVerdict.NOT_SUPPORTED_YET, result.verdict)
        assertTrue(result.missingInputs.contains("Country or tax jurisdiction"))
        assertTrue(result.summary.contains("not implemented"))
    }

    private fun planningSummary(): AnalyticsSummary {
        return FinancialAnalyticsEngine().summarize(
            listOf(
                transaction(TransactionType.CREDIT, 150_000_00, TransactionCategory.SALARY, "Salary"),
                transaction(TransactionType.DEBIT, 45_000_00, TransactionCategory.RENT, "Rent"),
                transaction(TransactionType.DEBIT, 15_000_00, TransactionCategory.GROCERIES, "DMart"),
                transaction(TransactionType.DEBIT, 10_000_00, TransactionCategory.FUEL, "Shell"),
                transaction(TransactionType.DEBIT, 20_000_00, TransactionCategory.FOOD, "Swiggy"),
                transaction(TransactionType.DEBIT, 30_000_00, TransactionCategory.INVESTMENT, "Groww SIP"),
            ),
        )
    }

    private fun transaction(
        type: TransactionType,
        amountMinor: Long,
        category: TransactionCategory,
        merchant: String,
    ): Transaction {
        return Transaction(
            type = type,
            amountMinor = amountMinor,
            merchantName = merchant,
            category = category,
            paymentMethod = PaymentMethod.UPI,
            accountLast4 = "1234",
            transactionTime = null,
            sourcePackage = "test",
            confidence = 0.95f,
            verificationStatus = VerificationStatus.AUTO_VERIFIED,
        )
    }

    private fun goals(): List<SpendingGoal> {
        return listOf(
            SpendingGoal(
                name = "Emergency Fund",
                targetAmountMinor = 300_000_00,
                currentAmountMinor = 100_000_00,
                targetDateEpochMillis = null,
            ),
            SpendingGoal(
                name = "Laptop",
                targetAmountMinor = 180_000_00,
                currentAmountMinor = 32_000_00,
                targetDateEpochMillis = null,
            ),
        )
    }
}
