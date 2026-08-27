package com.spendsense.features.finance.domain

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialAnalyticsEngineTest {
    private val engine = FinancialAnalyticsEngine()

    @Test
    fun summarizesSpendingLikeFinancialCoach() {
        val summary = engine.summarize(
            listOf(
                transaction(
                    id = "salary",
                    type = TransactionType.CREDIT,
                    amountMinor = 200_000_00,
                    category = TransactionCategory.SALARY,
                    merchant = "Salary",
                ),
                transaction(
                    id = "rent",
                    amountMinor = 60_000_00,
                    category = TransactionCategory.RENT,
                    merchant = "Landlord",
                ),
                transaction(
                    id = "food",
                    amountMinor = 12_000_00,
                    category = TransactionCategory.FOOD,
                    merchant = "Swiggy",
                ),
                transaction(
                    id = "shopping",
                    amountMinor = 20_000_00,
                    category = TransactionCategory.SHOPPING,
                    merchant = "Amazon",
                ),
                transaction(
                    id = "sip",
                    amountMinor = 30_000_00,
                    category = TransactionCategory.INVESTMENT,
                    merchant = "Groww SIP",
                ),
            ),
        )

        assertEquals(122_000_00, summary.totalExpenseMinor)
        assertEquals(200_000_00, summary.totalIncomeMinor)
        assertEquals(78_000_00, summary.netCashFlowMinor)
        assertEquals(54, summary.cashFlowAssessment.savingsRatePercent)
        assertTrue(summary.budgetHealth.score >= 80)
        assertTrue(summary.spendingSegments.any { it.role == ExpenseRole.SAVINGS_INVESTMENT && it.amountMinor == 30_000_00L })

        val shoppingOpportunity = summary.savingsOpportunities.first { it.title == "Trim Shopping" }
        assertEquals(4_000_00, shoppingOpportunity.potentialSavingMinor)

        val investmentRecommendation = summary.categoryRecommendations.first { it.category == TransactionCategory.INVESTMENT }
        assertEquals(ExpenseRole.SAVINGS_INVESTMENT, investmentRecommendation.role)
        assertTrue(investmentRecommendation.action.contains("Protect this"))
    }

    @Test
    fun recentSpendIgnoresOldMonthlyRentForSevenDayInsight() {
        val now = Instant.now()
        val summary = engine.summarize(
            listOf(
                transaction(
                    id = "salary",
                    type = TransactionType.CREDIT,
                    amountMinor = 200_000_00,
                    category = TransactionCategory.SALARY,
                    merchant = "Salary",
                    transactionTime = now.minusSeconds(86_400),
                ),
                transaction(
                    id = "old-rent",
                    amountMinor = 60_000_00,
                    category = TransactionCategory.RENT,
                    merchant = "Landlord",
                    transactionTime = now.minusSeconds(86_400 * 12),
                ),
                transaction(
                    id = "food-today",
                    amountMinor = 1_200_00,
                    category = TransactionCategory.FOOD,
                    merchant = "Swiggy",
                    transactionTime = now,
                ),
                transaction(
                    id = "grocery-yesterday",
                    amountMinor = 2_000_00,
                    category = TransactionCategory.GROCERIES,
                    merchant = "DMart",
                    transactionTime = now.minusSeconds(86_400),
                ),
                transaction(
                    id = "previous-food",
                    amountMinor = 400_00,
                    category = TransactionCategory.FOOD,
                    merchant = "Zomato",
                    transactionTime = now.minusSeconds(86_400 * 9),
                ),
            ),
        )

        assertEquals(63_600_00, summary.totalExpenseMinor)
        assertEquals(3_200_00, summary.recentSpend.last7DaysSpendMinor)
        assertEquals(TransactionCategory.GROCERIES, summary.recentSpend.topRecentCategories.first().category)
        assertTrue(summary.topCategories.first().category == TransactionCategory.RENT)

        val foodTrend = summary.recentSpend.categoryTrends.first { it.category == TransactionCategory.FOOD }
        assertEquals(800_00, foodTrend.deltaMinor)
        assertEquals(TrendDirection.UP, foodTrend.direction)

        val rentTrend = summary.recentSpend.categoryTrends.first { it.category == TransactionCategory.RENT }
        assertEquals(TrendDirection.DOWN, rentTrend.direction)
    }

    private fun transaction(
        id: String,
        type: TransactionType = TransactionType.DEBIT,
        amountMinor: Long,
        category: TransactionCategory,
        merchant: String,
        transactionTime: Instant = Instant.now(),
    ): Transaction {
        return Transaction(
            id = id,
            type = type,
            amountMinor = amountMinor,
            merchantName = merchant,
            category = category,
            paymentMethod = PaymentMethod.UPI,
            accountLast4 = "1234",
            transactionTime = transactionTime,
            sourcePackage = "test",
            confidence = 0.95f,
            verificationStatus = VerificationStatus.AUTO_VERIFIED,
        )
    }
}
