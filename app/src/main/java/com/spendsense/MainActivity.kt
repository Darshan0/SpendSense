package com.spendsense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.spendsense.core.presentation.SpendSenseTheme
import com.spendsense.features.finance.data.AppGraph
import com.spendsense.features.finance.presentation.SpendSenseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SpendSenseTheme {
                SpendSenseApp(
                    repository = AppGraph.transactionRepository,
                    goalRepository = AppGraph.spendingGoalRepository,
                    processNotification = AppGraph.processNotificationUseCase,
                    observeAnalyticsSummaryUseCase = AppGraph.observeAnalyticsSummaryUseCase,
                    generateInsightsUseCase = AppGraph.generateInsightsUseCase,
                    askAssistantUseCase = AppGraph.askAssistantUseCase,
                    userProfileRepository = AppGraph.userProfileRepository,
                    exportTransactionsUseCase = AppGraph.exportTransactionsUseCase,
                    transactionExportFileWriter = AppGraph.transactionExportFileWriter,
                )
            }
        }
    }
}
