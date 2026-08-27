package com.spendsense.features.finance.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AskAssistantUseCaseTest {
    @Test
    fun rejectsGenericContradictoryModelAnswer() = runTest {
        val useCase = AskAssistantUseCase(
            analytics = ObserveAnalyticsSummaryUseCase(FakeAnalyticsRepository()),
            goals = FakeSpendingGoalRepository(),
            languageModel = FakeLanguageModel(
                "Use the highest amount for your spending goals. Maximize spending."
            ),
        )

        val message = useCase.ask(
            question = "Where should I cut back to reach my goals?",
            summary = fuelSummary(),
        )

        assertTrue(message.answer.contains("Start with Fuel"))
        assertTrue(message.answer.contains("SHELL PETROLEUM"))
        assertTrue(message.answer.contains("Emergency Fund"))
        assertTrue(message.answer.contains("Source: Local analytics coach"))
        assertFalse(message.answer.contains("Maximize spending"))
    }

    @Test
    fun acceptsGroundedModelAnswer() = runTest {
        val useCase = AskAssistantUseCase(
            analytics = ObserveAnalyticsSummaryUseCase(FakeAnalyticsRepository()),
            goals = FakeSpendingGoalRepository(),
            languageModel = FakeLanguageModel(
                "Fuel is your first cut-back area at ₹428. SHELL PETROLEUM is the top merchant, so cap that spend before moving money to a goal."
            ),
        )

        val message = useCase.ask(
            question = "Where should I cut back to reach my goals?",
            summary = fuelSummary(),
        )

        assertTrue(message.answer.contains("Source: Bundled LiteRT-LM on device"))
        assertTrue(message.answer.contains("Fuel is your first cut-back area"))
    }

    @Test
    fun answersCategorySpendQuestionsWithCategoryTotal() = runTest {
        val useCase = AskAssistantUseCase(
            analytics = ObserveAnalyticsSummaryUseCase(FakeAnalyticsRepository()),
            goals = FakeSpendingGoalRepository(),
            languageModel = FakeLanguageModel("Here is a generic answer that does not include the requested category total."),
        )

        val message = useCase.ask(
            question = "How much did I spend on food?",
            summary = foodSummary(),
        )

        assertTrue(message.answer.contains("Food spend is ₹428"))
        assertTrue(message.answer.contains("100% of your tracked expenses"))
        assertTrue(message.answer.contains("Source: Local analytics coach"))
        assertFalse(message.answer.contains("Start with Fuel"))
    }

    @Test
    fun usesLatestPrecomputedSummaryWhenUiDoesNotPassSnapshot() = runTest {
        val useCase = AskAssistantUseCase(
            analytics = ObserveAnalyticsSummaryUseCase(FakeAnalyticsRepository(foodSummary())),
            goals = FakeSpendingGoalRepository(),
            languageModel = FakeLanguageModel("Here is a generic answer that does not include the requested category total."),
        )

        val message = useCase.ask(question = "How much did I spend on food?")

        assertTrue(message.answer.contains("Food spend is ₹428"))
        assertTrue(message.answer.contains("Source: Local analytics coach"))
    }

    @Test
    fun acceptsGroundedCasualAdvisorAnswer() = runTest {
        val languageModel = FakeLanguageModel(
            "I get why it feels stressful. Your budget health is Stable at 72/100, and discretionary spend is the first area I would watch before changing fixed bills."
        )
        val useCase = AskAssistantUseCase(
            analytics = ObserveAnalyticsSummaryUseCase(FakeAnalyticsRepository()),
            goals = FakeSpendingGoalRepository(),
            languageModel = languageModel,
        )

        val message = useCase.ask(
            question = "I feel like I am spending too much lately",
            summary = advisorSummary(),
            conversationHistory = listOf(
                ConversationTurn(ConversationRole.USER, "Can you help me understand my money?"),
                ConversationTurn(ConversationRole.ASSISTANT, "Yes, I can use your local spending data."),
            ),
        )

        assertTrue(message.answer.contains("I get why it feels stressful"))
        assertTrue(message.answer.contains("Source: Bundled LiteRT-LM on device"))
        assertTrue(languageModel.lastRequest?.intent == AssistantIntent.CASUAL_REFLECTION)
        assertTrue(languageModel.lastRequest?.conversationHistory?.size == 2)
    }

    @Test
    fun routesLoanQuestionThroughPlanningEngine() = runTest {
        val languageModel = FakeLanguageModel("The estimated EMI is ₹21,247, so this loan is caution territory unless you keep post-EMI savings healthy.")
        val useCase = AskAssistantUseCase(
            analytics = ObserveAnalyticsSummaryUseCase(FakeAnalyticsRepository()),
            goals = FakeSpendingGoalRepository(),
            languageModel = languageModel,
        )

        val message = useCase.ask(
            question = "Can I afford a ₹10 lakh loan at 10% for 5 years?",
            summary = advisorSummary(),
        )

        assertTrue(languageModel.lastRequest?.intent == AssistantIntent.LOAN_PLANNING)
        assertTrue(languageModel.lastRequest?.context?.contains("type=LOAN_AFFORDABILITY") == true)
        assertTrue(languageModel.lastRequest?.context?.contains("Estimated EMI") == true)
        assertTrue(message.answer.contains("Source: Bundled LiteRT-LM on device"))
    }

    @Test
    fun taxQuestionFallsBackToMissingInputsNotFabricatedAdvice() = runTest {
        val useCase = AskAssistantUseCase(
            analytics = ObserveAnalyticsSummaryUseCase(FakeAnalyticsRepository()),
            goals = FakeSpendingGoalRepository(),
            languageModel = FakeLanguageModel("Invest in random products and you will save all tax."),
        )

        val message = useCase.ask(
            question = "How can I save tax?",
            summary = advisorSummary(),
        )

        assertTrue(message.answer.contains("Tax planning needs jurisdiction-specific rules"))
        assertTrue(message.answer.contains("Country or tax jurisdiction"))
        assertTrue(message.answer.contains("Source: Local analytics coach"))
        assertFalse(message.answer.contains("save all tax"))
    }

    private fun fuelSummary(): AnalyticsSummary {
        return AnalyticsSummary(
            totalExpenseMinor = 42800,
            totalIncomeMinor = 27_500_000,
            netCashFlowMinor = 27_457_200,
            categorySpends = listOf(CategorySpend(TransactionCategory.FUEL, 42800)),
            merchantSpends = listOf(MerchantSpend("SHELL PETROLEUM", 42800)),
            topCategories = listOf(CategorySpend(TransactionCategory.FUEL, 42800)),
            topMerchants = listOf(MerchantSpend("SHELL PETROLEUM", 42800)),
            topDiscretionaryMerchants = listOf(MerchantSpend("SHELL PETROLEUM", 42800)),
            transactionCount = 2,
        )
    }

    private fun foodSummary(): AnalyticsSummary {
        return AnalyticsSummary(
            totalExpenseMinor = 42800,
            totalIncomeMinor = 27_500_000,
            netCashFlowMinor = 27_457_200,
            categorySpends = listOf(CategorySpend(TransactionCategory.FOOD, 42800)),
            merchantSpends = listOf(MerchantSpend("SWIGGY", 42800)),
            topCategories = listOf(CategorySpend(TransactionCategory.FOOD, 42800)),
            topMerchants = listOf(MerchantSpend("SWIGGY", 42800)),
            topDiscretionaryMerchants = listOf(MerchantSpend("SWIGGY", 42800)),
            transactionCount = 2,
        )
    }

    private fun advisorSummary(): AnalyticsSummary {
        return AnalyticsSummary(
            totalExpenseMinor = 80_000_00,
            totalIncomeMinor = 150_000_00,
            netCashFlowMinor = 70_000_00,
            categorySpends = listOf(
                CategorySpend(TransactionCategory.RENT, 45_000_00),
                CategorySpend(TransactionCategory.FOOD, 20_000_00),
                CategorySpend(TransactionCategory.SHOPPING, 15_000_00),
            ),
            merchantSpends = listOf(
                MerchantSpend("Prestige Lakeside", 45_000_00),
                MerchantSpend("Swiggy", 20_000_00),
                MerchantSpend("Amazon", 15_000_00),
            ),
            topCategories = listOf(
                CategorySpend(TransactionCategory.RENT, 45_000_00),
                CategorySpend(TransactionCategory.FOOD, 20_000_00),
                CategorySpend(TransactionCategory.SHOPPING, 15_000_00),
            ),
            topMerchants = listOf(
                MerchantSpend("Prestige Lakeside", 45_000_00),
                MerchantSpend("Swiggy", 20_000_00),
                MerchantSpend("Amazon", 15_000_00),
            ),
            topDiscretionaryMerchants = listOf(
                MerchantSpend("Swiggy", 20_000_00),
                MerchantSpend("Amazon", 15_000_00),
            ),
            transactionCount = 5,
            budgetHealth = BudgetHealth(
                score = 72,
                status = "Stable",
                explanation = "Your budget is workable, but category caps would make goal progress more predictable.",
            ),
            cashFlowAssessment = CashFlowAssessment(
                expenseRatioPercent = 53,
                savingsRatePercent = 46,
                fixedCostRatioPercent = 30,
                discretionaryRatioPercent = 23,
                investmentRatePercent = 0,
            ),
            savingsOpportunities = listOf(
                SavingsOpportunity(
                    title = "Trim Food",
                    category = TransactionCategory.FOOD,
                    merchantName = null,
                    currentAmountMinor = 20_000_00,
                    suggestedCutPercent = 20,
                    potentialSavingMinor = 4_000_00,
                    rationale = "Food is flexible enough to cap without changing fixed bills.",
                ),
            ),
        )
    }
}

private class FakeLanguageModel(
    private val response: String,
) : LocalLanguageModel {
    var lastRequest: LlmRequest? = null
        private set

    override suspend fun isAvailable(): Boolean = true

    override suspend fun status(): LlmStatus {
        return LlmStatus(
            provider = "Bundled LiteRT-LM",
            state = LlmAvailabilityState.AVAILABLE,
            detail = "Ready",
        )
    }

    override suspend fun generate(request: LlmRequest): LlmResult {
        lastRequest = request
        return LlmResult(
            text = response,
            provider = "Bundled LiteRT-LM",
            usedFallback = false,
        )
    }
}

private class FakeAnalyticsRepository(
    private val summary: AnalyticsSummary = AnalyticsSummary(
        totalExpenseMinor = 0,
        totalIncomeMinor = 0,
        netCashFlowMinor = 0,
        topCategories = emptyList(),
        topMerchants = emptyList(),
        transactionCount = 0,
    ),
) : AnalyticsRepository {
    override fun observeSummary(): Flow<AnalyticsSummary> = flowOf(summary)

    override suspend fun currentSummary(): AnalyticsSummary = summary
}

private class FakeSpendingGoalRepository : SpendingGoalRepository {
    override fun observeGoals(): Flow<List<SpendingGoal>> {
        return flowOf(
            listOf(
                SpendingGoal(
                    name = "Emergency Fund",
                    targetAmountMinor = 10000000,
                    currentAmountMinor = 2750000,
                    targetDateEpochMillis = null,
                ),
            ),
        )
    }

    override suspend fun insert(goal: SpendingGoal) = Unit

    override suspend fun delete(id: String) = Unit
}
