package com.spendsense.features.finance.data

import android.content.Context
import com.spendsense.features.finance.domain.AskAssistantUseCase
import com.spendsense.features.finance.domain.ExportTransactionsUseCase
import com.spendsense.features.finance.domain.GenerateInsightsUseCase
import com.spendsense.features.finance.domain.ObserveAnalyticsSummaryUseCase
import com.spendsense.features.finance.domain.ProcessNotificationUseCase
import com.spendsense.features.finance.domain.SpendingGoalRepository
import com.spendsense.features.finance.domain.TransactionRepository
import com.spendsense.features.finance.domain.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object AppGraph {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var database: SpendSenseDatabase
    private lateinit var roomTransactionRepository: RoomTransactionRepository
    private lateinit var roomSpendingGoalRepository: RoomSpendingGoalRepository
    private lateinit var precomputedAnalyticsRepository: PrecomputedAnalyticsRepository
    private lateinit var localUserProfileRepository: LocalUserProfileRepository

    val transactionRepository: TransactionRepository
        get() = roomTransactionRepository

    val spendingGoalRepository: SpendingGoalRepository
        get() = roomSpendingGoalRepository

    val userProfileRepository: UserProfileRepository
        get() = localUserProfileRepository

    private val sanitizer = DefaultNotificationSanitizer()
    private val sensitiveMessageFilter = RegexSensitiveMessageFilter()
    private val classifier = HeuristicFinancialClassifier()
    private val parser = GenericFinancialParser()
    private val validator = DefaultExtractionValidator()
    private lateinit var languageModel: LocalLanguageModelRouter

    lateinit var processNotificationUseCase: ProcessNotificationUseCase
        private set
    lateinit var observeAnalyticsSummaryUseCase: ObserveAnalyticsSummaryUseCase
        private set
    lateinit var generateInsightsUseCase: GenerateInsightsUseCase
        private set
    lateinit var askAssistantUseCase: AskAssistantUseCase
        private set
    lateinit var exportTransactionsUseCase: ExportTransactionsUseCase
        private set
    lateinit var transactionExportFileWriter: TransactionExportFileWriter
        private set

    fun initialize(context: Context) {
        if (::database.isInitialized) return

        database = SpendSenseDatabase.create(context)
        localUserProfileRepository = LocalUserProfileRepository(context)
        transactionExportFileWriter = TransactionExportFileWriter(context.applicationContext)
        exportTransactionsUseCase = ExportTransactionsUseCase()
        languageModel = LocalLanguageModelRouter(
            providers = listOf(
                PackagedLiteRtLanguageModel(context.applicationContext),
                GeminiNanoLanguageModel(),
            ),
            fallback = NoOpLanguageModel(),
        )
        roomTransactionRepository = RoomTransactionRepository(database.transactionDao())
        roomSpendingGoalRepository = RoomSpendingGoalRepository(database.spendingGoalDao())
        precomputedAnalyticsRepository = PrecomputedAnalyticsRepository(
            transactionRepository = roomTransactionRepository,
            appScope = appScope,
        )
        observeAnalyticsSummaryUseCase = ObserveAnalyticsSummaryUseCase(precomputedAnalyticsRepository)
        generateInsightsUseCase = GenerateInsightsUseCase(
            analytics = observeAnalyticsSummaryUseCase,
            goals = roomSpendingGoalRepository,
        )
        askAssistantUseCase = AskAssistantUseCase(
            analytics = observeAnalyticsSummaryUseCase,
            goals = roomSpendingGoalRepository,
            languageModel = languageModel,
        )
        processNotificationUseCase = ProcessNotificationUseCase(
            sanitizer = sanitizer,
            sensitiveMessageFilter = sensitiveMessageFilter,
            classifier = classifier,
            parser = parser,
            validator = validator,
            repository = roomTransactionRepository,
        )

        appScope.launch {
            roomTransactionRepository.seedDevelopmentData()
            roomSpendingGoalRepository.seedDevelopmentData()
        }
        appScope.launch {
            runCatching { languageModel.prepare() }
        }
    }
}
