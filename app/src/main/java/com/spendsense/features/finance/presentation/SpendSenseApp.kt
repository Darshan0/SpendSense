package com.spendsense.features.finance.presentation

import android.content.Intent
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spendsense.core.presentation.MoneyFormatter
import com.spendsense.features.finance.data.NotificationAccess
import com.spendsense.features.finance.data.TransactionExportFileWriter
import com.spendsense.features.finance.domain.AnalyticsSummary
import com.spendsense.features.finance.domain.AskAssistantUseCase
import com.spendsense.features.finance.domain.CategoryTrend
import com.spendsense.features.finance.domain.ConversationRole
import com.spendsense.features.finance.domain.ConversationTurn
import com.spendsense.features.finance.domain.ExportTransactionsUseCase
import com.spendsense.features.finance.domain.FinancialInsight
import com.spendsense.features.finance.domain.GenerateInsightsUseCase
import com.spendsense.features.finance.domain.LlmAvailabilityState
import com.spendsense.features.finance.domain.LlmStatus
import com.spendsense.features.finance.domain.NotificationProcessor
import com.spendsense.features.finance.domain.ObserveAnalyticsSummaryUseCase
import com.spendsense.features.finance.domain.ProcessingResult
import com.spendsense.features.finance.domain.RawNotification
import com.spendsense.features.finance.domain.SpendingGoal
import com.spendsense.features.finance.domain.SpendingGoalRepository
import com.spendsense.features.finance.domain.Transaction
import com.spendsense.features.finance.domain.TransactionCategory
import com.spendsense.features.finance.domain.TransactionRepository
import com.spendsense.features.finance.domain.TransactionType
import com.spendsense.features.finance.domain.TrendDirection
import com.spendsense.features.finance.domain.UserProfile
import com.spendsense.features.finance.domain.UserProfileRepository
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private enum class AppTab(val label: String) {
    HOME("Home"),
    TRANSACTIONS("History"),
    AI("AI"),
    INSIGHTS("Insights"),
    GOALS("Profile"),
}

private enum class ChatAuthor {
    USER,
    ASSISTANT,
}

private data class ChatMessage(
    val author: ChatAuthor,
    val text: String,
    val visual: ChatVisual? = null,
)

private data class ChatVisual(
    val title: String,
    val metric: String,
    val detail: String,
    val progress: Float,
    val isPositive: Boolean,
    val category: TransactionCategory? = null,
)

private data class ChatSuggestion(
    val label: String,
    val question: String,
    val answer: String,
    val visual: ChatVisual,
)

private val NamiBackground = Color(0xFF131315)
private val NamiSurface = Color(0xFF1B1B1D)
private val NamiSurfaceHigh = Color(0xFF2A2A2C)
private val NamiSurfaceHighest = Color(0xFF353437)
private val NamiPrimaryContainer = Color(0xFF5E5CE6)
private val NamiPrimary = Color(0xFFC2C1FF)
private val NamiTertiary = Color(0xFFAAC7FF)
private val NamiOutline = Color(0xFF918FA0)
private val NamiOnSurface = Color(0xFFE4E2E4)
private val NamiOnSurfaceVariant = Color(0xFFC7C4D7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendSenseApp(
    repository: TransactionRepository,
    goalRepository: SpendingGoalRepository,
    processNotification: NotificationProcessor,
    observeAnalyticsSummaryUseCase: ObserveAnalyticsSummaryUseCase,
    generateInsightsUseCase: GenerateInsightsUseCase,
    askAssistantUseCase: AskAssistantUseCase,
    userProfileRepository: UserProfileRepository,
    exportTransactionsUseCase: ExportTransactionsUseCase,
    transactionExportFileWriter: TransactionExportFileWriter,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val transactions by repository.observeTransactions().collectAsStateWithLifecycle(initialValue = emptyList())
    val goals by goalRepository.observeGoals().collectAsStateWithLifecycle(initialValue = emptyList())
    val profile by userProfileRepository.observeProfile().collectAsStateWithLifecycle(initialValue = UserProfile())
    val analytics by observeAnalyticsSummaryUseCase.observe().collectAsStateWithLifecycle(initialValue = null)
    val insights by generateInsightsUseCase.observe().collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        topBar = {
            NamiTopBar()
        },
        bottomBar = {
            NamiBottomBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
        },
        containerColor = NamiBackground,
    ) { contentPadding ->
        when (selectedTab) {
            AppTab.HOME -> HomeScreen(
                transactions = transactions,
                goals = goals,
                analytics = analytics,
                modifier = Modifier.padding(contentPadding),
            )
            AppTab.TRANSACTIONS -> TransactionsScreen(
                transactions = transactions,
                processNotification = processNotification,
                modifier = Modifier.padding(contentPadding),
            )
            AppTab.INSIGHTS -> InsightsScreen(
                analytics = analytics,
                insights = insights,
                modifier = Modifier.padding(contentPadding),
            )
            AppTab.GOALS -> GoalsScreen(
                profile = profile,
                goals = goals,
                analytics = analytics,
                transactions = transactions,
                onProfileSave = { updatedProfile ->
                    scope.launch { userProfileRepository.update(updatedProfile) }
                },
                onAddGoal = { goal ->
                    scope.launch { goalRepository.insert(goal) }
                },
                onDeleteGoal = { goal ->
                    scope.launch { goalRepository.delete(goal.id) }
                },
                onExport = {
                    val currentAnalytics = analytics ?: return@GoalsScreen
                    val csv = exportTransactionsUseCase.buildCsv(transactions, currentAnalytics)
                    val intent = transactionExportFileWriter.writeCsv(csv)
                    context.startActivity(Intent.createChooser(intent, "Export SpendSense analysis"))
                },
                onOpenNotificationSettings = {
                    context.startActivity(NotificationAccess.settingsIntent())
                },
                modifier = Modifier.padding(contentPadding),
            )
            AppTab.AI -> AssistantChatScreen(
                askAssistantUseCase = askAssistantUseCase,
                analytics = analytics,
                goals = goals,
                modifier = Modifier.padding(contentPadding),
            )
        }
    }
}

@Composable
private fun HomeScreen(
    transactions: List<Transaction>,
    goals: List<SpendingGoal>,
    analytics: AnalyticsSummary?,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NamiBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HomeHeader()
        }
        item {
            RecentFocusPanel(analytics = analytics, goal = goals.firstOrNull())
        }
        item {
            RecentCategoryPressurePanel(analytics)
        }
        item {
            GoalImpactPanel(analytics = analytics, goal = goals.firstOrNull())
        }
        item {
            DailyTrendPanel(analytics)
        }
        item {
            MonthlyContextPanel(analytics)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("Recent transactions")
                Text("Grouped", color = NamiPrimary, style = MaterialTheme.typography.labelMedium)
            }
        }
        if (transactions.isEmpty()) {
            item { EmptyPanel("No recent transactions", "When SpendSense captures a new spend, this page will explain the day, category, and goal impact.") }
        } else {
            item {
                TransactionGroupList(transactions = transactions.take(8))
            }
        }
    }
}

@Composable
private fun InsightsScreen(
    analytics: AnalyticsSummary?,
    insights: List<FinancialInsight>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NamiBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionTitle("Advisor insights")
        }
        if (analytics == null) {
            item {
                EmptyPanel("No advisor signal yet", "SpendSense needs recent transactions before it can compare patterns and suggest actions.")
            }
        } else {
            item {
                AdvisorPulsePanel(analytics)
            }
            item {
                CategoryMovementStrip(analytics.recentSpend.categoryTrends)
            }
            item {
                SectionTitle("What to do now")
            }
            val signals = buildAdvisorSignals(analytics, insights)
            if (signals.isEmpty()) {
                item {
                    EmptyPanel("No action needed yet", "Recent spend is not showing a strong warning. Keep capturing data so SpendSense can catch changes early.")
                }
            } else {
                items(signals, key = { it.title }) { signal ->
                    AdvisorActionCard(signal)
                }
            }
        }
    }
}

@Composable
private fun AdvisorPulsePanel(analytics: AnalyticsSummary) {
    val recent = analytics.recentSpend
    val dailyBudget = recent.suggestedDailyBudgetMinor
    val todayProgress = if (dailyBudget <= 0L) 0f else (recent.todaySpendMinor.toFloat() / dailyBudget.toFloat()).coerceIn(0f, 1.4f)
    val topRiser = recent.categoryTrends.firstOrNull { it.direction == TrendDirection.UP && it.current7DaysMinor > 0L }
    val isOverGuardrail = dailyBudget > 0L && recent.todaySpendMinor > dailyBudget
    val title = when {
        isOverGuardrail -> "Slow spending today"
        topRiser != null -> "${topRiser.category.displayName()} is moving up"
        recent.last7DaysSpendMinor > 0L -> "Recent spend is under watch"
        else -> "Waiting for recent activity"
    }
    val body = when {
        isOverGuardrail -> "Today is ${MoneyFormatter.formatMinor(recent.todaySpendMinor, "INR")} against a ${MoneyFormatter.formatMinor(dailyBudget, "INR")} guardrail. Pause flexible purchases for the rest of the day."
        topRiser != null -> "${topRiser.category.displayName()} increased by ${MoneyFormatter.formatMinor(topRiser.deltaMinor, "INR")} versus the previous 7 days. This is the category to control first."
        recent.last7DaysSpendMinor > 0L -> "The last 7 days total is ${MoneyFormatter.formatMinor(recent.last7DaysSpendMinor, "INR")}. Watch daily spikes before monthly bills hide the pattern."
        else -> "No spend was captured in the last 7 days, so there is nothing useful to warn about yet."
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, NamiPrimaryContainer.copy(alpha = 0.22f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            if (isOverGuardrail) Color(0xFFFF6B6B).copy(alpha = 0.18f) else NamiPrimaryContainer.copy(alpha = 0.18f),
                            NamiSurface.copy(alpha = 0.96f),
                        ),
                    ),
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CategoryIcon(topRiser?.category ?: TransactionCategory.INVESTMENT, size = 44.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = NamiOnSurface, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Text("Based on last 7 days vs previous 7 days", color = NamiPrimary, style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(body, color = NamiOnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                FocusMetric("Today", MoneyFormatter.formatMinor(recent.todaySpendMinor, "INR"), Modifier.weight(1f))
                FocusMetric("7 days", MoneyFormatter.formatMinor(recent.last7DaysSpendMinor, "INR"), Modifier.weight(1f))
            }
            if (dailyBudget > 0L) {
                LinearProgressIndicator(
                    progress = { todayProgress.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().clip(CircleShape),
                    color = if (isOverGuardrail) Color(0xFFFF6B6B) else Color(0xFF64D6A3),
                    trackColor = NamiSurfaceHighest,
                )
            }
        }
    }
}

@Composable
private fun CategoryMovementStrip(trends: List<CategoryTrend>) {
    val visibleTrends = trends.filter { it.current7DaysMinor > 0L || it.previous7DaysMinor > 0L }.take(4)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Category movement")
        if (visibleTrends.isEmpty()) {
            EmptyPanel("No category movement", "New transactions will show which categories are rising or cooling down.")
        } else {
            MilledCard {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    visibleTrends.forEach { trend ->
                        CategoryMovementRow(trend)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryMovementRow(trend: CategoryTrend) {
    val color = if (trend.direction == TrendDirection.DOWN) Color(0xFF64D6A3) else if (trend.direction == TrendDirection.UP) Color(0xFFFF6B6B) else NamiOutline
    val progressBase = maxOf(trend.current7DaysMinor, trend.previous7DaysMinor, 1L)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(trend.category, size = 38.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(trend.category.displayName(), color = NamiOnSurface, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (trend.direction == TrendDirection.DOWN) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(15.dp),
                    )
                    Text("${kotlin.math.abs(trend.deltaPercent)}%", color = color, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                }
            }
            LinearProgressIndicator(
                progress = { (trend.current7DaysMinor.toFloat() / progressBase.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().clip(CircleShape),
                color = color,
                trackColor = NamiSurfaceHighest,
            )
            Text(
                "${MoneyFormatter.formatMinor(trend.current7DaysMinor, "INR")} now vs ${MoneyFormatter.formatMinor(trend.previous7DaysMinor, "INR")} before",
                color = NamiOutline,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

private data class AdvisorSignal(
    val title: String,
    val body: String,
    val metric: String,
    val progress: Float,
    val category: TransactionCategory?,
    val isPositive: Boolean,
)

@Composable
private fun AdvisorActionCard(signal: AdvisorSignal) {
    val color = if (signal.isPositive) Color(0xFF64D6A3) else Color(0xFFFF6B6B)
    MilledCard {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CategoryIcon(signal.category ?: TransactionCategory.OTHER, size = 40.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(signal.title, color = NamiOnSurface, fontWeight = FontWeight.SemiBold)
                    Text(signal.body, color = NamiOnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Icon(
                        imageVector = if (signal.isPositive) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(signal.metric, color = color, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                }
            }
            LinearProgressIndicator(
                progress = { signal.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().clip(CircleShape),
                color = color,
                trackColor = NamiSurfaceHighest,
            )
        }
    }
}

private fun buildAdvisorSignals(
    analytics: AnalyticsSummary,
    insights: List<FinancialInsight>,
): List<AdvisorSignal> {
    val recent = analytics.recentSpend
    val signals = mutableListOf<AdvisorSignal>()
    val tobaccoMerchant = recent.topRecentMerchants.firstOrNull { merchant ->
        val name = merchant.merchantName.lowercase()
        listOf("cigarette", "cig", "tobacco", "smoke", "pan", "gutka").any { name.contains(it) }
    }
    if (tobaccoMerchant != null) {
        signals += AdvisorSignal(
            title = "Stop tobacco spend",
            body = "${tobaccoMerchant.merchantName} appears in recent spends. This is not a budget category to optimize; it is a habit to cut.",
            metric = MoneyFormatter.formatMinor(tobaccoMerchant.amountMinor, "INR"),
            progress = tobaccoMerchant.amountMinor.toFloat() / recent.last7DaysSpendMinor.coerceAtLeast(1L).toFloat(),
            category = TransactionCategory.OTHER,
            isPositive = false,
        )
    }

    val risingFood = recent.categoryTrends.firstOrNull {
        it.category == TransactionCategory.FOOD && it.direction == TrendDirection.UP && it.current7DaysMinor > 0L
    }
    if (risingFood != null) {
        signals += AdvisorSignal(
            title = "Eating out is increasing",
            body = "Food is up versus the previous 7 days. Set a meal cap today before it becomes the weekly pattern.",
            metric = "+${MoneyFormatter.formatMinor(risingFood.deltaMinor, "INR")}",
            progress = trendProgress(risingFood),
            category = TransactionCategory.FOOD,
            isPositive = false,
        )
    }

    val risingFlexible = recent.categoryTrends.firstOrNull {
        it.direction == TrendDirection.UP &&
            it.current7DaysMinor > 0L &&
            it.category != TransactionCategory.FOOD &&
            it.category.isFlexibleSpend()
    }
    if (risingFlexible != null) {
        signals += AdvisorSignal(
            title = "${risingFlexible.category.displayName()} needs a cap",
            body = "This flexible category is rising faster than last week. Pause one purchase cycle and move that money to your goal.",
            metric = "+${MoneyFormatter.formatMinor(risingFlexible.deltaMinor, "INR")}",
            progress = trendProgress(risingFlexible),
            category = risingFlexible.category,
            isPositive = false,
        )
    }

    val investment = recent.investmentTrend
    if (analytics.totalIncomeMinor > 0L && (investment == null || investment.current7DaysMinor <= investment.previous7DaysMinor)) {
        signals += AdvisorSignal(
            title = "Investment momentum is weak",
            body = "Investment transfers are not increasing. Protect a small automatic transfer before shopping, food, or entertainment spends.",
            metric = MoneyFormatter.formatMinor(investment?.current7DaysMinor ?: 0L, "INR"),
            progress = if (analytics.totalIncomeMinor <= 0L) 0f else ((investment?.current7DaysMinor ?: 0L).toFloat() / analytics.totalIncomeMinor.toFloat()),
            category = TransactionCategory.INVESTMENT,
            isPositive = false,
        )
    } else if (investment != null && investment.current7DaysMinor > investment.previous7DaysMinor) {
        signals += AdvisorSignal(
            title = "Investments are improving",
            body = "You invested more than the previous 7 days. Keep this transfer protected before discretionary spending.",
            metric = "+${MoneyFormatter.formatMinor(investment.deltaMinor, "INR")}",
            progress = trendProgress(investment),
            category = TransactionCategory.INVESTMENT,
            isPositive = true,
        )
    }

    val dailyBudget = recent.suggestedDailyBudgetMinor
    if (dailyBudget > 0L && recent.todaySpendMinor > dailyBudget) {
        signals += AdvisorSignal(
            title = "Today is over budget",
            body = "Today crossed the daily guardrail. Avoid new flexible spends until tomorrow so the week does not compound.",
            metric = MoneyFormatter.formatMinor(recent.todaySpendMinor - dailyBudget, "INR"),
            progress = recent.todaySpendMinor.toFloat() / dailyBudget.toFloat(),
            category = recent.topRecentCategories.firstOrNull()?.category,
            isPositive = false,
        )
    }

    val improvingCategory = recent.categoryTrends.firstOrNull {
        it.direction == TrendDirection.DOWN && it.previous7DaysMinor > 0L && it.category.isFlexibleSpend()
    }
    if (signals.size < 4 && improvingCategory != null) {
        signals += AdvisorSignal(
            title = "${improvingCategory.category.displayName()} is cooling down",
            body = "This is the right direction. Keep the same cap and redirect the difference into savings or investment.",
            metric = "-${MoneyFormatter.formatMinor(kotlin.math.abs(improvingCategory.deltaMinor), "INR")}",
            progress = trendProgress(improvingCategory),
            category = improvingCategory.category,
            isPositive = true,
        )
    }

    if (signals.isEmpty()) {
        insights.take(2).mapTo(signals) { insight ->
            AdvisorSignal(
                title = insight.title,
                body = insight.body,
                metric = analytics.budgetHealth.status,
                progress = analytics.budgetHealth.score / 100f,
                category = analytics.recentSpend.topRecentCategories.firstOrNull()?.category,
                isPositive = insight.severity != com.spendsense.features.finance.domain.InsightSeverity.WARNING,
            )
        }
    }

    return signals.distinctBy { it.title }.take(4)
}

private fun trendProgress(trend: CategoryTrend): Float {
    val base = maxOf(trend.current7DaysMinor, trend.previous7DaysMinor, 1L)
    return (kotlin.math.abs(trend.deltaMinor).toFloat() / base.toFloat()).coerceIn(0f, 1f)
}

private fun TransactionCategory.isFlexibleSpend(): Boolean {
    return when (this) {
        TransactionCategory.FOOD,
        TransactionCategory.SHOPPING,
        TransactionCategory.ENTERTAINMENT,
        TransactionCategory.TRAVEL,
        TransactionCategory.SUBSCRIPTION,
        TransactionCategory.OTHER -> true
        else -> false
    }
}

@Composable
private fun GoalsScreen(
    profile: UserProfile,
    goals: List<SpendingGoal>,
    analytics: AnalyticsSummary?,
    transactions: List<Transaction>,
    onProfileSave: (UserProfile) -> Unit,
    onAddGoal: (SpendingGoal) -> Unit,
    onDeleteGoal: (SpendingGoal) -> Unit,
    onExport: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NamiBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ProfileHeader(
                profile = profile,
                onProfileSave = onProfileSave,
            )
        }
        item {
            ProfileFinancialPanel(
                profile = profile,
                analytics = analytics,
                onProfileSave = onProfileSave,
            )
        }
        item {
            ExportPanel(
                transactions = transactions,
                analytics = analytics,
                onExport = onExport,
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SectionTitle("Goals")
                Text("${goals.size} active", color = NamiPrimary, style = MaterialTheme.typography.labelMedium)
            }
        }
        item {
            AddGoalPanel(onAddGoal)
        }
        if (goals.isEmpty()) {
            item {
                EmptyPanel("No goals yet", "Create goals to turn spending analysis into a plan.")
            }
        } else {
            items(goals, key = { it.id }) { goal ->
                GoalCard(goal = goal, onDelete = { onDeleteGoal(goal) })
            }
        }
        item {
            ProfileSettingsPanel(
                onOpenNotificationSettings = onOpenNotificationSettings,
            )
        }
        item {
            AccountPanel()
        }
    }
}

@Composable
private fun ProfileHeader(
    profile: UserProfile,
    onProfileSave: (UserProfile) -> Unit,
) {
    val context = LocalContext.current
    var name by remember(profile.name) { mutableStateOf(profile.name) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        val savedPath = uri?.let { copyProfilePhoto(context, it) }
        if (savedPath != null) {
            onProfileSave(profile.copy(profilePhotoPath = savedPath))
        }
    }

    MilledCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(profile = profile)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(profile.name.ifBlank { "Your profile" }, color = NamiOnSurface, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Text("Goals, salary, export, and app controls", color = NamiOnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = { photoPicker.launch("image/*") }) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = "Change profile photo", tint = NamiPrimary)
                }
            }
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                singleLine = true,
                colors = profileTextFieldColors(),
            )
            AssistChip(
                onClick = { onProfileSave(profile.copy(name = name.ifBlank { "Darshan" })) },
                label = { Text("Save profile") },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
            )
        }
    }
}

@Composable
private fun ProfileAvatar(profile: UserProfile) {
    val bitmap = remember(profile.profilePhotoPath) {
        profile.profilePhotoPath
            ?.let(::File)
            ?.takeIf { it.exists() }
            ?.let { BitmapFactory.decodeFile(it.absolutePath)?.asImageBitmap() }
    }
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(NamiPrimaryContainer.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Profile photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = profile.name.trim().take(1).ifBlank { "S" }.uppercase(),
                color = NamiPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}

@Composable
private fun ProfileFinancialPanel(
    profile: UserProfile,
    analytics: AnalyticsSummary?,
    onProfileSave: (UserProfile) -> Unit,
) {
    var salaryText by remember(profile.monthlySalaryMinor) {
        mutableStateOf(if (profile.monthlySalaryMinor > 0L) (profile.monthlySalaryMinor / 100L).toString() else "")
    }
    val salaryMinor = salaryText.toRupeeMinor()
    val trackedIncome = analytics?.totalIncomeMinor ?: 0L
    MilledCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                CategoryIcon(TransactionCategory.SALARY, size = 40.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Income profile", color = NamiOnSurface, fontWeight = FontWeight.SemiBold)
                    Text("Used for daily budget and goal planning", color = NamiOnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
            TextField(
                value = salaryText,
                onValueChange = { salaryText = it.filter { char -> char.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monthly salary") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = profileTextFieldColors(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                FocusMetric("Saved salary", MoneyFormatter.formatMinor(profile.monthlySalaryMinor, profile.currency), Modifier.weight(1f))
                FocusMetric("Tracked income", MoneyFormatter.formatMinor(trackedIncome, profile.currency), Modifier.weight(1f))
            }
            AssistChip(
                enabled = salaryMinor >= 0L,
                onClick = { onProfileSave(profile.copy(monthlySalaryMinor = salaryMinor)) },
                label = { Text("Save salary") },
                leadingIcon = { Icon(Icons.Filled.Savings, contentDescription = null) },
            )
        }
    }
}

@Composable
private fun ExportPanel(
    transactions: List<Transaction>,
    analytics: AnalyticsSummary?,
    onExport: () -> Unit,
) {
    MilledCard {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(TransactionCategory.TRANSFER, size = 40.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Export analysis", color = NamiOnSurface, fontWeight = FontWeight.SemiBold)
                Text(
                    "${transactions.size} transactions with category share, weekly movement, and advisor notes.",
                    color = NamiOnSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(
                enabled = analytics != null && transactions.isNotEmpty(),
                onClick = onExport,
            ) {
                Icon(Icons.Filled.Download, contentDescription = "Export transactions", tint = if (analytics != null && transactions.isNotEmpty()) NamiPrimary else NamiOutline)
            }
        }
    }
}

@Composable
private fun AddGoalPanel(onAddGoal: (SpendingGoal) -> Unit) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var current by remember { mutableStateOf("") }
    val targetMinor = target.toRupeeMinor()
    val currentMinor = current.toRupeeMinor()
    val canSave = name.isNotBlank() && targetMinor > 0L

    MilledCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                CategoryIcon(TransactionCategory.INVESTMENT, size = 38.dp)
                Text("Add goal", color = NamiOnSurface, fontWeight = FontWeight.SemiBold)
            }
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Goal name") },
                singleLine = true,
                colors = profileTextFieldColors(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = target,
                    onValueChange = { target = it.filter { char -> char.isDigit() } },
                    modifier = Modifier.weight(1f),
                    label = { Text("Target") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = profileTextFieldColors(),
                )
                TextField(
                    value = current,
                    onValueChange = { current = it.filter { char -> char.isDigit() } },
                    modifier = Modifier.weight(1f),
                    label = { Text("Saved") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = profileTextFieldColors(),
                )
            }
            AssistChip(
                enabled = canSave,
                onClick = {
                    onAddGoal(
                        SpendingGoal(
                            name = name.trim(),
                            targetAmountMinor = targetMinor,
                            currentAmountMinor = currentMinor.coerceAtMost(targetMinor),
                            targetDateEpochMillis = null,
                        ),
                    )
                    name = ""
                    target = ""
                    current = ""
                },
                label = { Text("Add goal") },
                leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
            )
        }
    }
}

@Composable
private fun ProfileSettingsPanel(onOpenNotificationSettings: () -> Unit) {
    ActionPanel(
        title = "Notification access",
        body = "Manage the permission that lets SpendSense capture bank and wallet transaction alerts.",
        action = "Open settings",
        onAction = onOpenNotificationSettings,
    )
}

@Composable
private fun AccountPanel() {
    MilledCard {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(TransactionCategory.OTHER, size = 40.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text("Account", color = NamiOnSurface, fontWeight = FontWeight.SemiBold)
                Text("Logout will connect here when cloud auth is added.", color = NamiOnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            AssistChip(
                enabled = false,
                onClick = {},
                label = { Text("Logout") },
            )
        }
    }
}

@Composable
private fun AssistantChatScreen(
    askAssistantUseCase: AskAssistantUseCase,
    analytics: AnalyticsSummary?,
    goals: List<SpendingGoal>,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var draft by remember { mutableStateOf("") }
    var isAsking by remember { mutableStateOf(false) }
    val suggestions = remember(analytics, goals) {
        buildChatSuggestions(analytics, goals.firstOrNull())
    }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                ChatAuthor.ASSISTANT,
                "Pick a recent-spend question below, or ask your own.",
            ),
        )
    }

    fun askPrecomputed(suggestion: ChatSuggestion) {
        if (isAsking) return
        messages += ChatMessage(ChatAuthor.USER, suggestion.question)
        messages += ChatMessage(
            author = ChatAuthor.ASSISTANT,
            text = suggestion.answer,
            visual = suggestion.visual,
        )
    }

    fun ask(question: String) {
        val trimmed = question.trim()
        if (trimmed.isBlank() || isAsking) return

        messages += ChatMessage(ChatAuthor.USER, trimmed)
        draft = ""
        isAsking = true
        val recentHistory = messages
            .takeLast(8)
            .map { message ->
                ConversationTurn(
                    role = if (message.author == ChatAuthor.USER) ConversationRole.USER else ConversationRole.ASSISTANT,
                    text = message.text,
                )
            }
        scope.launch {
            val answer = askAssistantUseCase.ask(
                question = trimmed,
                conversationHistory = recentHistory,
            )
            messages += ChatMessage(
                author = ChatAuthor.ASSISTANT,
                text = answer.answer,
                visual = analytics?.let { buildAnswerVisual(trimmed, it, goals.firstOrNull()) },
            )
            isAsking = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NamiBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ChatInsightHeader(analytics = analytics, goal = goals.firstOrNull())

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 10.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }
            if (isAsking) {
                item {
                    ChatBubble(
                        ChatMessage(
                            author = ChatAuthor.ASSISTANT,
                            text = "Reading the latest cached spend summary...",
                            visual = analytics?.let { buildAnswerVisual("cash flow", it, goals.firstOrNull()) },
                        ),
                    )
                }
            }
        }

        QuickPromptRow(
            enabled = !isAsking && analytics != null,
            suggestions = suggestions,
            onPrompt = ::askPrecomputed,
        )

        ChatComposer(
            value = draft,
            enabled = !isAsking && analytics != null,
            onValueChange = { draft = it },
            onSend = { ask(draft) },
        )
    }
}

private fun llmStatusText(status: LlmStatus?): String {
    if (status == null) return "Checking local model"
    val state = when (status.state) {
        LlmAvailabilityState.AVAILABLE -> "Ready"
        LlmAvailabilityState.DOWNLOADABLE -> "Available on first use"
        LlmAvailabilityState.DOWNLOADING -> "Downloading"
        LlmAvailabilityState.UNAVAILABLE -> "Unavailable"
        LlmAvailabilityState.NOT_INSTALLED -> "Not installed"
        LlmAvailabilityState.ERROR -> "Error"
    }
    return "${status.provider}: $state"
}

@Composable
private fun TransactionsScreen(
    transactions: List<Transaction>,
    processNotification: NotificationProcessor,
    modifier: Modifier = Modifier,
) {
    var status by remember { mutableStateOf<String?>(null) }
    val sampleNotification = remember {
        RawNotification(
            key = "debug-sample-${Instant.now().toEpochMilli()}",
            packageName = "debug.sample",
            title = "HDFC Bank",
            text = "INR 680.00 debited from A/c XX8831 via UPI to SHELL PETROLEUM. Avl Bal INR 41,228.21.",
            bigText = null,
            subText = null,
            postedAt = Instant.now().toEpochMilli(),
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NamiBackground)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 18.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionTitle("Transactions")
            ActionPanel(
                title = "Notification pipeline",
                body = "Validate parsing, sensitive-data filtering, and local persistence with a sample bank message.",
                action = "Add sample",
                onAction = { status = "Processing sample..." },
            )
        }
        item {
            SampleProcessor(
                status = status,
                sampleNotification = sampleNotification,
                processNotification = processNotification,
                onStatus = { status = it },
            )
        }
        status?.let { value ->
            item {
                StatusPanel(value)
            }
        }
        if (transactions.isEmpty()) {
            item {
                EmptyPanel("No transactions yet", "Use the sample action or enable notification access to start the pipeline.")
            }
        } else {
            groupedTransactions(transactions).forEach { group ->
                item(key = group.label) {
                    TransactionGroupCard(group)
                }
            }
        }
    }
}

@Composable
private fun SampleProcessor(
    status: String?,
    sampleNotification: RawNotification,
    processNotification: NotificationProcessor,
    onStatus: (String) -> Unit,
) {
    LaunchedEffect(status) {
        if (status == "Processing sample...") {
            val result = processNotification.process(
                sampleNotification.copy(key = "debug-sample-${Instant.now().toEpochMilli()}"),
            )
            onStatus(result.toUserText())
        }
    }
}

private fun ProcessingResult.toUserText(): String {
    return when (this) {
        is ProcessingResult.Ignored -> "Ignored: $reason"
        is ProcessingResult.Failed -> "Failed: $reason"
        is ProcessingResult.NeedsReview -> "Needs review before saving"
        is ProcessingResult.Saved -> "Saved ${transaction.merchantName ?: "transaction"}"
    }
}

@Composable
private fun NamiTopBar() {
    Surface(
        color = NamiBackground.copy(alpha = 0.94f),
        contentColor = NamiOnSurface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = NamiPrimary)
                Column {
                    Text("SpendSense", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Today and recent spend", style = MaterialTheme.typography.labelSmall, color = NamiOutline)
                }
            }
            Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = NamiPrimary)
        }
    }
}

@Composable
private fun SecureChannelChip() {
    Surface(
        color = NamiSurface,
        contentColor = NamiOnSurfaceVariant,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = CircleShape,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Security, contentDescription = null, tint = NamiPrimary, modifier = Modifier.size(14.dp))
            Text("SECURE", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun NamiBottomBar(selectedTab: AppTab, onTabSelected: (AppTab) -> Unit) {
    NavigationBar(
        containerColor = NamiSurface.copy(alpha = 0.96f),
        tonalElevation = 0.dp,
    ) {
        AppTab.entries.forEach { tab ->
            val selected = selectedTab == tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                label = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(tab.label, style = MaterialTheme.typography.labelSmall)
                        if (selected) {
                            Spacer(
                                modifier = Modifier
                                    .padding(top = 3.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(NamiPrimaryContainer),
                            )
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            AppTab.HOME -> Icons.Filled.Home
                            AppTab.TRANSACTIONS -> Icons.AutoMirrored.Filled.List
                            AppTab.AI -> Icons.Filled.SmartToy
                            AppTab.INSIGHTS -> Icons.Filled.Insights
                            AppTab.GOALS -> Icons.Filled.Person
                        },
                        contentDescription = tab.label,
                        tint = if (selected) NamiPrimaryContainer else NamiOnSurfaceVariant,
                    )
                },
            )
        }
    }
}

@Composable
private fun HomeHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = "Good morning, Darshan",
            style = MaterialTheme.typography.bodyMedium,
            color = NamiOnSurfaceVariant.copy(alpha = 0.72f),
        )
        Text(
            text = "Here is what changed recently.",
            style = MaterialTheme.typography.headlineSmall,
            color = NamiOnSurface,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RecentFocusPanel(analytics: AnalyticsSummary?, goal: SpendingGoal?) {
    val recent = analytics?.recentSpend
    val topCategory = recent?.topRecentCategories?.firstOrNull()
    val goalRemaining = goal?.let { (it.targetAmountMinor - it.currentAmountMinor).coerceAtLeast(0L) }
    val goalImpact = if (goal != null && goalRemaining != null && recent != null && recent.last7DaysSpendMinor > 0L) {
        val share = ((recent.last7DaysSpendMinor * 100L) / goalRemaining.coerceAtLeast(1L)).coerceAtMost(999L)
        "That equals $share% of the remaining ${goal.name} gap."
    } else {
        "Add a goal to see how recent spend changes your runway."
    }
    val title = when {
        recent == null || recent.last7DaysSpendMinor == 0L -> "No recent spend yet"
        topCategory != null -> "${topCategory.category.displayName()} drove the last 7 days"
        else -> "Recent spend is ready"
    }
    val body = if (recent == null || recent.last7DaysSpendMinor == 0L) {
        "When new transactions arrive, SpendSense will compare today, yesterday, and the last 7 days instead of over-weighting monthly bills."
    } else {
        val categoryText = topCategory?.let {
            "${it.category.displayName()} is ${MoneyFormatter.formatMinor(it.amountMinor, "INR")} of the last 7 days. "
        }.orEmpty()
        "${categoryText}You spent ${MoneyFormatter.formatMinor(recent.last7DaysSpendMinor, "INR")} recently, averaging ${MoneyFormatter.formatMinor(recent.dailyAverageMinor, "INR")} per day. $goalImpact"
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, NamiPrimaryContainer.copy(alpha = 0.24f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            NamiPrimaryContainer.copy(alpha = 0.18f),
                            Color(0xFF2E4F4F).copy(alpha = 0.24f),
                            NamiSurface.copy(alpha = 0.96f),
                        ),
                    ),
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CategoryIcon(topCategory?.category ?: TransactionCategory.OTHER, size = 42.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = NamiOnSurface, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Text("Recent window, not just monthly bills", color = NamiPrimary, style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(body, color = NamiOnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                FocusMetric("Today", MoneyFormatter.formatMinor(recent?.todaySpendMinor ?: 0L, "INR"), Modifier.weight(1f))
                FocusMetric("Yesterday", MoneyFormatter.formatMinor(recent?.yesterdaySpendMinor ?: 0L, "INR"), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun FocusMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = NamiOutline, style = MaterialTheme.typography.labelSmall)
            Text(value, color = NamiOnSurface, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RecentCategoryPressurePanel(analytics: AnalyticsSummary?) {
    val recent = analytics?.recentSpend
    val categories = recent?.topRecentCategories.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Where money went this week")
        if (categories.isEmpty()) {
            EmptyPanel("No 7-day category signal", "No recent expenses were captured in the last 7 days. Monthly categories are hidden here until they become useful.")
        } else {
            MilledCard {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    categories.take(4).forEach { category ->
                        CategoryPressureRow(
                            category = category,
                            totalMinor = recent?.last7DaysSpendMinor ?: 0L,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPressureRow(category: com.spendsense.features.finance.domain.CategorySpend, totalMinor: Long) {
    val share = if (totalMinor <= 0L) 0 else ((category.amountMinor * 100L) / totalMinor).toInt()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(category.category, size = 40.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(category.category.displayName(), color = NamiOnSurface, fontWeight = FontWeight.Medium)
                Text("${share}%", color = category.category.accentColor(), fontWeight = FontWeight.SemiBold)
            }
            LinearProgressIndicator(
                progress = { (share / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape),
                color = category.category.accentColor(),
                trackColor = NamiSurfaceHighest,
            )
        }
        Text(MoneyFormatter.formatMinor(category.amountMinor, "INR"), color = NamiOnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun GoalImpactPanel(analytics: AnalyticsSummary?, goal: SpendingGoal?) {
    val recent = analytics?.recentSpend
    if (goal == null) {
        EmptyPanel("Goal impact unavailable", "Add a savings goal so recent spend can be translated into days delayed or monthly pressure.")
        return
    }
    val remaining = (goal.targetAmountMinor - goal.currentAmountMinor).coerceAtLeast(0L)
    val recentSpend = recent?.last7DaysSpendMinor ?: 0L
    val dailyAverage = recent?.dailyAverageMinor ?: 0L
    val daysWorth = if (dailyAverage > 0L) recentSpend / dailyAverage else 0L
    val progress = if (goal.targetAmountMinor <= 0L) 0f else (goal.currentAmountMinor.toFloat() / goal.targetAmountMinor.toFloat()).coerceIn(0f, 1f)

    MilledCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryIcon(TransactionCategory.INVESTMENT, size = 40.dp)
                    Column {
                        Text(goal.name, color = NamiOnSurface, fontWeight = FontWeight.SemiBold)
                        Text("${MoneyFormatter.formatMinor(remaining, goal.currency)} left", color = NamiOnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text("${(progress * 100).toInt()}%", color = NamiPrimary, fontWeight = FontWeight.SemiBold)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().clip(CircleShape),
                color = NamiPrimaryContainer,
                trackColor = NamiSurfaceHighest,
            )
            Text(
                text = if (recentSpend > 0L) {
                    "Last 7 days spend was ${MoneyFormatter.formatMinor(recentSpend, "INR")}. Holding just 15% of that would add ${MoneyFormatter.formatMinor((recentSpend * 15L) / 100L, "INR")} to this goal."
                } else {
                    "No recent spend captured, so your goal runway did not change from new transactions this week."
                },
                color = NamiOnSurfaceVariant,
            )
            if (daysWorth > 0L) {
                Text("$daysWorth days of current recent spending is visible in this goal decision.", color = NamiOutline, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun DailyTrendPanel(analytics: AnalyticsSummary?) {
    val trend = analytics?.recentSpend?.dailyTrend.orEmpty()
    val max = trend.maxOfOrNull { it.amountMinor }?.coerceAtLeast(1L) ?: 1L
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle("Daily spend trend")
        MilledCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 128.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    val bars = if (trend.isEmpty()) List(7) { index -> com.spendsense.features.finance.domain.DailySpend(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")[index], 0L) } else trend
                    bars.forEachIndexed { index, day ->
                        val height = (20 + ((day.amountMinor * 100L) / max).toInt()).dp
                        val isToday = index == bars.lastIndex
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = height)
                                    .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                    .background(if (isToday) NamiPrimaryContainer else NamiTertiary.copy(alpha = 0.28f)),
                            )
                            Text(day.label.take(3), color = if (isToday) NamiPrimary else NamiOutline, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                val yesterday = analytics?.recentSpend?.yesterdaySpendMinor ?: 0L
                Text(
                    text = if (yesterday > 0L) {
                        "Yesterday was ${MoneyFormatter.formatMinor(yesterday, "INR")}. Use this chart to spot repeat spikes, not one-off rent."
                    } else {
                        "No spend yesterday. The next captured transaction will make this trend more useful."
                    },
                    color = NamiOnSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun MonthlyContextPanel(analytics: AnalyticsSummary?) {
    val topMonthly = analytics?.topCategories?.firstOrNull()
    MilledCard {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(topMonthly?.category ?: TransactionCategory.OTHER, size = 40.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text("Monthly context", color = NamiOnSurface, fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (topMonthly != null) {
                        "${topMonthly.category.displayName()} is still the largest monthly category at ${MoneyFormatter.formatMinor(topMonthly.amountMinor, "INR")}, but daily decisions are tracked above."
                    } else {
                        "Monthly totals will appear after income and expenses are captured."
                    },
                    color = NamiOnSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun AiInsightPanel(analytics: AnalyticsSummary?) {
    val topCategory = analytics?.topCategories?.firstOrNull()
    val message = if (topCategory != null && analytics.totalExpenseMinor > 0L) {
        val share = ((topCategory.amountMinor * 100L) / analytics.totalExpenseMinor).toInt()
        "${topCategory.category.displayName()} is your biggest category at $share% of tracked expenses. Ask AI what to cut next."
    } else {
        "Capture a few transactions and I will turn them into category, cash-flow, and goal insights."
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, NamiPrimaryContainer.copy(alpha = 0.22f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            NamiPrimaryContainer.copy(alpha = 0.14f),
                            NamiTertiary.copy(alpha = 0.18f),
                            NamiPrimaryContainer.copy(alpha = 0.12f),
                        ),
                    ),
                )
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NamiPrimaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.SmartToy, contentDescription = null, tint = Color.White)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                Text("AI Insight", style = MaterialTheme.typography.labelSmall, color = NamiPrimary, fontWeight = FontWeight.SemiBold)
                Text(message, style = MaterialTheme.typography.bodyLarge, color = NamiOnSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SpendingOverviewPanel(analytics: AnalyticsSummary?) {
    val spent = analytics?.totalExpenseMinor ?: 0L
    val income = analytics?.totalIncomeMinor ?: 0L
    val progress = if (income <= 0L) 0f else (spent.toFloat() / income.toFloat()).coerceIn(0f, 1f)
    val remaining = (income - spent).coerceAtLeast(0L)

    MilledCard {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text("Spent this month", style = MaterialTheme.typography.labelSmall, color = NamiOnSurfaceVariant)
                    Text(
                        MoneyFormatter.formatMinor(spent, "INR"),
                        style = MaterialTheme.typography.headlineSmall,
                        color = NamiOnSurface,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Remaining", style = MaterialTheme.typography.labelSmall, color = NamiOnSurfaceVariant)
                    Text(
                        MoneyFormatter.formatMinor(remaining, "INR"),
                        style = MaterialTheme.typography.bodyLarge,
                        color = NamiPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 8.dp)
                    .clip(CircleShape),
                color = NamiPrimaryContainer,
                trackColor = NamiSurfaceHighest,
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${(progress * 100).toInt()}% of income", style = MaterialTheme.typography.labelSmall, color = NamiOutline)
                Text("${analytics?.transactionCount ?: 0} txns tracked", style = MaterialTheme.typography.labelSmall, color = NamiOutline)
            }
        }
    }
}

@Composable
private fun SpendingTrendPanel(analytics: AnalyticsSummary?) {
    val amounts = analytics?.topCategories?.map { it.amountMinor }?.take(7).orEmpty()
    val bars = if (amounts.isEmpty()) listOf(40L, 62L, 32L, 74L, 50L, 68L, 82L) else amounts
    val max = bars.maxOrNull()?.coerceAtLeast(1L) ?: 1L

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Spending Trend")
        MilledCard {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 132.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    bars.forEachIndexed { index, amount ->
                        val height = (44 + ((amount * 88) / max).toInt()).dp
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = height)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (index == bars.lastIndex) NamiPrimaryContainer else NamiPrimary.copy(alpha = 0.2f)),
                        )
                    }
                }
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = NamiOutline.copy(alpha = 0.72f))
                    }
                }
            }
        }
    }
}

@Composable
private fun NamiGoalCard(goal: SpendingGoal) {
    val progress = if (goal.targetAmountMinor <= 0L) 0f else (goal.currentAmountMinor.toFloat() / goal.targetAmountMinor.toFloat()).coerceIn(0f, 1f)
    MilledCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(NamiTertiary.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Flag, contentDescription = null, tint = NamiTertiary)
                }
                Column {
                    Text(goal.name, color = NamiOnSurface, fontWeight = FontWeight.SemiBold)
                    Text(
                        "${MoneyFormatter.formatMinor(goal.currentAmountMinor, goal.currency)} / ${MoneyFormatter.formatMinor(goal.targetAmountMinor, goal.currency)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NamiOnSurfaceVariant,
                    )
                }
            }
            Text("${(progress * 100).toInt()}%", color = NamiOnSurface, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CategoryBento(analytics: AnalyticsSummary?) {
    val categories = analytics?.topCategories.orEmpty().take(2)
    if (categories.isEmpty()) return

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        categories.forEach { category ->
            MilledCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(category.category.accentColor().copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(category.category.name.take(1), color = category.category.accentColor(), fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text(category.category.displayName(), style = MaterialTheme.typography.labelSmall, color = NamiOnSurfaceVariant)
                        Text(
                            MoneyFormatter.formatMinor(category.amountMinor, "INR"),
                            style = MaterialTheme.typography.titleMedium,
                            color = NamiOnSurface,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MilledCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = NamiSurface,
        contentColor = NamiOnSurface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        content = content,
    )
}

@Composable
private fun HeroPanel(analytics: AnalyticsSummary?) {
    Surface(
        color = NamiPrimaryContainer,
        contentColor = NamiOnSurface,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "Financial cockpit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Private spend intelligence from notifications, goals, and a local AI model.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = MoneyFormatter.formatMinor(analytics?.netCashFlowMinor ?: 0L, "INR"),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Current tracked cash flow",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, modifier: Modifier = Modifier) {
    MilledCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = NamiOnSurface,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun EmptyPanel(title: String, body: String) {
    MilledCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, color = NamiOnSurface)
            Text(body, color = NamiOnSurfaceVariant)
        }
    }
}

@Composable
private fun GoalCard(goal: SpendingGoal, onDelete: (() -> Unit)? = null) {
    val progress = if (goal.targetAmountMinor <= 0L) {
        0f
    } else {
        (goal.currentAmountMinor.toFloat() / goal.targetAmountMinor.toFloat()).coerceIn(0f, 1f)
    }
    MilledCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.name, fontWeight = FontWeight.SemiBold, color = NamiOnSurface)
                    Text("${(progress * 100).toInt()}% funded", color = NamiPrimary, style = MaterialTheme.typography.labelMedium)
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete goal", tint = Color(0xFFFF6B6B))
                    }
                } else {
                    Text("${(progress * 100).toInt()}%", color = NamiPrimary)
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape),
                color = NamiPrimaryContainer,
                trackColor = NamiSurfaceHighest,
            )
            Text(
                text = "${MoneyFormatter.formatMinor(goal.currentAmountMinor, goal.currency)} of ${MoneyFormatter.formatMinor(goal.targetAmountMinor, goal.currency)}",
                color = NamiOnSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private data class TransactionDateGroup(
    val label: String,
    val transactions: List<Transaction>,
)

@Composable
private fun TransactionGroupList(transactions: List<Transaction>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        groupedTransactions(transactions).forEach { group ->
            TransactionGroupCard(group)
        }
    }
}

@Composable
private fun TransactionGroupCard(group: TransactionDateGroup) {
    MilledCard {
        Column(modifier = Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(group.label, color = NamiOnSurface, fontWeight = FontWeight.SemiBold)
                Text(
                    MoneyFormatter.formatMinor(group.transactions.filterOutflows().sumOf { it.amountMinor }, "INR"),
                    color = NamiOutline,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            group.transactions.forEach { transaction ->
                TransactionListRow(transaction)
            }
        }
    }
}

@Composable
private fun TransactionListRow(transaction: Transaction) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CategoryIcon(transaction.category, size = 42.dp)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = transaction.merchantName ?: "Unknown merchant",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    color = NamiOnSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = MoneyFormatter.formatMinor(transaction.amountMinor, transaction.currency, transaction.type),
                    fontWeight = FontWeight.SemiBold,
                    color = if (transaction.type == TransactionType.CREDIT || transaction.type == TransactionType.REFUND) {
                        Color(0xFF64D6A3)
                    } else {
                        NamiOnSurface
                    },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CategoryTag(transaction.category)
                Text("${(transaction.confidence * 100).toInt()}%", color = NamiOutline, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun CategoryIcon(category: TransactionCategory, size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(category.accentColor().copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = category.icon(),
            contentDescription = category.displayName(),
            tint = category.accentColor(),
            modifier = Modifier.size(size * 0.52f),
        )
    }
}

@Composable
private fun CategoryTag(category: TransactionCategory) {
    Surface(
        shape = CircleShape,
        color = category.accentColor().copy(alpha = 0.14f),
        contentColor = category.accentColor(),
    ) {
        Text(
            text = category.displayName(),
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun groupedTransactions(transactions: List<Transaction>): List<TransactionDateGroup> {
    val zone = ZoneId.systemDefault()
    return transactions
        .groupBy { (it.transactionTime ?: it.createdAt).atZone(zone).toLocalDate() }
        .toSortedMap(compareByDescending { it })
        .map { (date, items) ->
            TransactionDateGroup(
                label = date.relativeDateLabel(),
                transactions = items.sortedByDescending { it.transactionTime ?: it.createdAt },
            )
        }
}

private fun List<Transaction>.filterOutflows(): List<Transaction> {
    return filter { it.type == TransactionType.DEBIT || it.type == TransactionType.CASH_WITHDRAWAL }
}

private fun LocalDate.relativeDateLabel(): String {
    val today = LocalDate.now()
    return when (this) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> format(DateTimeFormatter.ofPattern("EEE, d MMM"))
    }
}

@Composable
private fun TransactionCard(transaction: Transaction) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = NamiSurface,
        contentColor = NamiOnSurface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NamiSurfaceHighest),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = transaction.category.name.take(1),
                        fontWeight = FontWeight.Bold,
                        color = transaction.category.accentColor(),
                    )
                }
                Column {
                    Text(
                        text = transaction.merchantName ?: "Unknown merchant",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium,
                        color = NamiOnSurface,
                    )
                    Text(
                        text = "${transaction.category.displayName()} - ${(transaction.confidence * 100).toInt()}% confidence",
                        color = NamiOnSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Text(
                text = MoneyFormatter.formatMinor(transaction.amountMinor, transaction.currency, transaction.type),
                fontWeight = FontWeight.SemiBold,
                color = if (transaction.type == TransactionType.CREDIT || transaction.type == TransactionType.REFUND) {
                    Color(0xFF64D6A3)
                } else {
                    NamiOnSurface
                },
            )
        }
    }
}

@Composable
private fun CategoryCard(label: String, amountMinor: Long, totalMinor: Long) {
    val progress = if (totalMinor <= 0L) 0f else (amountMinor.toFloat() / totalMinor.toFloat()).coerceIn(0f, 1f)
    MilledCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(label, fontWeight = FontWeight.Medium, color = NamiOnSurface)
                Text(MoneyFormatter.formatMinor(amountMinor, "INR"), color = NamiOnSurface)
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape),
                color = NamiPrimaryContainer,
                trackColor = NamiSurfaceHighest,
            )
        }
    }
}

@Composable
private fun InsightCard(insight: FinancialInsight) {
    MilledCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(insight.title, fontWeight = FontWeight.SemiBold, color = NamiOnSurface)
            Text(insight.body, color = NamiOnSurfaceVariant)
        }
    }
}

@Composable
private fun ChatInsightHeader(analytics: AnalyticsSummary?, goal: SpendingGoal?) {
    val recent = analytics?.recentSpend
    val topCategory = recent?.topRecentCategories?.firstOrNull()
    MilledCard {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryIcon(topCategory?.category ?: TransactionCategory.OTHER, size = 42.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Ask from recent data", color = NamiOnSurface, fontWeight = FontWeight.SemiBold)
                Text(
                    text = if (recent != null && recent.last7DaysSpendMinor > 0L) {
                        "${MoneyFormatter.formatMinor(recent.last7DaysSpendMinor, "INR")} in 7 days" +
                            goal?.let { " against ${it.name}" }.orEmpty()
                    } else {
                        "No recent spend signal yet"
                    },
                    color = NamiOnSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun AiStatusCard(status: LlmStatus?, transactionCount: Int) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            NamiSurfaceHigh.copy(alpha = 0.92f),
                            NamiSurface.copy(alpha = 0.92f),
                        ),
                    ),
                )
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(NamiPrimaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.SmartToy, contentDescription = null, tint = Color.White)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("AI chat", fontWeight = FontWeight.SemiBold, color = NamiOnSurface)
                    Text(
                        text = llmStatusText(status),
                        color = NamiOnSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            AssistChip(
                onClick = {},
                label = { Text("$transactionCount txns") },
            )
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.author == ChatAuthor.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, end = 10.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(NamiPrimaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.SmartToy, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Surface(
            modifier = Modifier.widthIn(max = if (isUser) 330.dp else 360.dp),
            color = if (isUser) NamiSurfaceHigh else Color.Transparent,
            contentColor = NamiOnSurface,
            shape = RoundedCornerShape(
                topStart = if (isUser) 18.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 18.dp,
                bottomStart = 18.dp,
                bottomEnd = 18.dp,
            ),
            border = if (isUser) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        ) {
            val background = if (isUser) {
                Brush.linearGradient(listOf(NamiSurfaceHigh, NamiSurfaceHigh))
            } else {
                Brush.linearGradient(
                    listOf(
                        NamiPrimaryContainer.copy(alpha = 0.12f),
                        NamiSurface.copy(alpha = 0.84f),
                    ),
                )
            }
            Column(
                modifier = Modifier
                    .background(background)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!isUser && message.visual != null) {
                    ChatVisualCard(message.visual)
                }
                Text(
                    text = if (isUser) message.text else message.text.compactAssistantText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NamiOnSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ChatVisualCard(visual: ChatVisual) {
    val color = if (visual.isPositive) Color(0xFF64D6A3) else Color(0xFFFF7A7A)
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    visual.category?.let { CategoryIcon(it, size = 30.dp) }
                    Text(visual.title, color = NamiOnSurface, fontWeight = FontWeight.SemiBold)
                }
                Icon(
                    imageVector = if (visual.isPositive) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(visual.metric, color = color, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
                progress = { visual.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().clip(CircleShape),
                color = color,
                trackColor = NamiSurfaceHighest,
            )
            Text(visual.detail, color = NamiOutline, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun SecureNote() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = NamiSurface,
            contentColor = NamiOutline,
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(13.dp))
                Text("All data is processed on-device", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun QuickPromptRow(
    enabled: Boolean,
    suggestions: List<ChatSuggestion>,
    onPrompt: (ChatSuggestion) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(suggestions, key = { it.question }) { suggestion ->
            AssistChip(
                enabled = enabled,
                onClick = { onPrompt(suggestion) },
                label = { Text(suggestion.label, maxLines = 1) },
                leadingIcon = {
                    suggestion.visual.category?.let {
                        Icon(it.icon(), contentDescription = null, tint = it.accentColor())
                    }
                },
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
            )
        }
    }
}

@Composable
private fun ChatComposer(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        shape = RoundedCornerShape(18.dp),
        color = NamiSurface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                placeholder = { Text("Ask SpendSense anything...") },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
            )
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (enabled && value.isNotBlank()) NamiPrimaryContainer else NamiSurfaceHighest,
                contentColor = Color.White,
            ) {
                IconButton(
                    enabled = enabled && value.isNotBlank(),
                    onClick = onSend,
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@Composable
private fun ActionPanel(
    title: String,
    body: String,
    action: String,
    onAction: () -> Unit,
) {
    MilledCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, fontWeight = FontWeight.SemiBold, color = NamiOnSurface)
            Text(body, color = NamiOnSurfaceVariant)
            AssistChip(
                onClick = onAction,
                label = { Text(action) },
                leadingIcon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
            )
        }
    }
}

@Composable
private fun StatusPanel(text: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = NamiSurfaceHigh,
        contentColor = NamiOnSurface,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun TransactionCategory.displayName(): String {
    return name.lowercase().replaceFirstChar { it.titlecase() }
}

private fun TransactionCategory.accentColor(): Color {
    return when (this) {
        TransactionCategory.FOOD,
        TransactionCategory.GROCERIES -> Color(0xFFFFB86B)
        TransactionCategory.TRAVEL,
        TransactionCategory.TRANSPORT,
        TransactionCategory.FUEL -> Color(0xFF79B8FF)
        TransactionCategory.SHOPPING,
        TransactionCategory.ENTERTAINMENT -> Color(0xFFE9A7FF)
        TransactionCategory.UTILITIES,
        TransactionCategory.RENT -> Color(0xFFFFD166)
        TransactionCategory.HEALTHCARE,
        TransactionCategory.EDUCATION -> Color(0xFF64D6A3)
        TransactionCategory.SUBSCRIPTION -> NamiPrimary
        TransactionCategory.INVESTMENT,
        TransactionCategory.SALARY -> Color(0xFF7EE0A1)
        TransactionCategory.TRANSFER,
        TransactionCategory.CASH,
            TransactionCategory.OTHER -> NamiOnSurfaceVariant
    }
}

private fun TransactionCategory.icon(): ImageVector {
    return when (this) {
        TransactionCategory.FOOD -> Icons.Filled.Restaurant
        TransactionCategory.GROCERIES -> Icons.Filled.LocalGroceryStore
        TransactionCategory.TRANSPORT,
        TransactionCategory.FUEL,
        TransactionCategory.TRAVEL -> Icons.Filled.DirectionsCar
        TransactionCategory.SHOPPING,
        TransactionCategory.ENTERTAINMENT -> Icons.Filled.ShoppingBag
        TransactionCategory.RENT -> Icons.Filled.HomeWork
        TransactionCategory.UTILITIES -> Icons.Filled.Home
        TransactionCategory.HEALTHCARE,
        TransactionCategory.EDUCATION -> Icons.Filled.Category
        TransactionCategory.SUBSCRIPTION -> Icons.AutoMirrored.Filled.ReceiptLong
        TransactionCategory.INVESTMENT,
        TransactionCategory.SALARY -> Icons.Filled.Savings
        TransactionCategory.TRANSFER,
        TransactionCategory.CASH -> Icons.Filled.AccountBalanceWallet
        TransactionCategory.OTHER -> Icons.Filled.Category
    }
}

private fun buildChatSuggestions(
    analytics: AnalyticsSummary?,
    goal: SpendingGoal?,
): List<ChatSuggestion> {
    if (analytics == null) return emptyList()

    val recent = analytics.recentSpend
    val topRecentCategory = recent.topRecentCategories.firstOrNull()
    val topRecentMerchant = recent.topRecentMerchants.firstOrNull()
    val dailyBudget = recent.suggestedDailyBudgetMinor
    val dailyProgress = if (dailyBudget <= 0L) 0f else recent.todaySpendMinor.toFloat() / dailyBudget.toFloat()
    val goalRemaining = goal?.let { (it.targetAmountMinor - it.currentAmountMinor).coerceAtLeast(0L) }
    val weeklyGoalShare = if (goalRemaining != null && goalRemaining > 0L) {
        recent.last7DaysSpendMinor.toFloat() / goalRemaining.toFloat()
    } else {
        0f
    }

    return listOf(
        ChatSuggestion(
            label = "7-day spend",
            question = "What changed in my last 7 days of spending?",
            answer = if (topRecentCategory != null) {
                "${topRecentCategory.category.displayName()} is the main recent driver at ${MoneyFormatter.formatMinor(topRecentCategory.amountMinor, "INR")}. The week total is ${MoneyFormatter.formatMinor(recent.last7DaysSpendMinor, "INR")}, averaging ${MoneyFormatter.formatMinor(recent.dailyAverageMinor, "INR")} per day."
            } else {
                "No spend was captured in the last 7 days. Once new transactions arrive, this will focus on recent categories instead of old monthly bills."
            },
            visual = ChatVisual(
                title = topRecentCategory?.category?.displayName() ?: "No recent spend",
                metric = MoneyFormatter.formatMinor(recent.last7DaysSpendMinor, "INR"),
                detail = "Last 7 days total",
                progress = if (analytics.totalExpenseMinor <= 0L) 0f else recent.last7DaysSpendMinor.toFloat() / analytics.totalExpenseMinor.toFloat(),
                isPositive = recent.last7DaysSpendMinor <= analytics.totalExpenseMinor / 4L,
                category = topRecentCategory?.category,
            ),
        ),
        ChatSuggestion(
            label = "Yesterday",
            question = "What drove yesterday's spend?",
            answer = if (recent.yesterdaySpendMinor > 0L) {
                "Yesterday was ${MoneyFormatter.formatMinor(recent.yesterdaySpendMinor, "INR")}. Compare that with your 7-day daily average of ${MoneyFormatter.formatMinor(recent.dailyAverageMinor, "INR")} to see whether it was a spike or normal rhythm."
            } else {
                "No spend was captured yesterday. That means yesterday did not add pressure to your weekly trend."
            },
            visual = ChatVisual(
                title = "Yesterday",
                metric = MoneyFormatter.formatMinor(recent.yesterdaySpendMinor, "INR"),
                detail = "Compared with ${MoneyFormatter.formatMinor(recent.dailyAverageMinor, "INR")} daily average",
                progress = if (recent.dailyAverageMinor <= 0L) 0f else recent.yesterdaySpendMinor.toFloat() / recent.dailyAverageMinor.toFloat(),
                isPositive = recent.yesterdaySpendMinor <= recent.dailyAverageMinor,
                category = topRecentCategory?.category,
            ),
        ),
        ChatSuggestion(
            label = "Daily budget",
            question = "Am I on track with today's daily spend?",
            answer = if (dailyBudget > 0L) {
                "Today is ${MoneyFormatter.formatMinor(recent.todaySpendMinor, "INR")} against a suggested daily spend guardrail of ${MoneyFormatter.formatMinor(dailyBudget, "INR")}. ${if (recent.todaySpendMinor <= dailyBudget) "You are inside the guardrail." else "You are above the guardrail, so pause flexible spends today."}"
            } else {
                "I need tracked income before setting a daily spend guardrail. Add or capture income so daily spending can be compared against a real budget."
            },
            visual = ChatVisual(
                title = "Today",
                metric = MoneyFormatter.formatMinor(recent.todaySpendMinor, "INR"),
                detail = if (dailyBudget > 0L) "Daily guardrail ${MoneyFormatter.formatMinor(dailyBudget, "INR")}" else "Income needed for guardrail",
                progress = dailyProgress,
                isPositive = dailyBudget > 0L && recent.todaySpendMinor <= dailyBudget,
                category = topRecentCategory?.category,
            ),
        ),
        ChatSuggestion(
            label = "Goal impact",
            question = "How did recent spend affect my goal?",
            answer = if (goal != null && goalRemaining != null && goalRemaining > 0L) {
                "Your last 7 days of spend equals ${((recent.last7DaysSpendMinor * 100L) / goalRemaining).coerceAtMost(999L)}% of the remaining ${goal.name} gap. Holding back 15% of recent spend would move ${MoneyFormatter.formatMinor((recent.last7DaysSpendMinor * 15L) / 100L, goal.currency)} toward the goal."
            } else {
                "Add an active goal and I can translate recent spend into goal pressure, saved days, and practical tradeoffs."
            },
            visual = ChatVisual(
                title = goal?.name ?: "Goal needed",
                metric = goalRemaining?.let { MoneyFormatter.formatMinor(it, goal.currency) } ?: "No goal",
                detail = "Remaining goal gap",
                progress = weeklyGoalShare,
                isPositive = weeklyGoalShare <= 0.10f,
                category = TransactionCategory.INVESTMENT,
            ),
        ),
        ChatSuggestion(
            label = "First action",
            question = "What is the first thing I should reduce?",
            answer = if (topRecentCategory != null) {
                "Start with ${topRecentCategory.category.displayName()} because it is the biggest recent category, not just a monthly fixed bill. A 15% reduction would free ${MoneyFormatter.formatMinor((topRecentCategory.amountMinor * 15L) / 100L, "INR")} from the current week pattern."
            } else {
                "There is no recent category pressure yet. Wait for a few new transactions before cutting anything."
            },
            visual = ChatVisual(
                title = topRecentCategory?.category?.displayName() ?: "Wait",
                metric = topRecentCategory?.let { MoneyFormatter.formatMinor((it.amountMinor * 15L) / 100L, "INR") } ?: "No cut",
                detail = topRecentMerchant?.let { "Watch ${it.merchantName}" } ?: "Need recent transactions",
                progress = if (recent.last7DaysSpendMinor <= 0L || topRecentCategory == null) 0f else topRecentCategory.amountMinor.toFloat() / recent.last7DaysSpendMinor.toFloat(),
                isPositive = false,
                category = topRecentCategory?.category,
            ),
        ),
    )
}

private fun buildAnswerVisual(
    question: String,
    analytics: AnalyticsSummary,
    goal: SpendingGoal?,
): ChatVisual {
    val normalized = question.lowercase()
    val recent = analytics.recentSpend
    val category = recent.topRecentCategories.firstOrNull()
    val dailyBudget = recent.suggestedDailyBudgetMinor
    return when {
        "goal" in normalized && goal != null -> {
            val remaining = (goal.targetAmountMinor - goal.currentAmountMinor).coerceAtLeast(0L)
            ChatVisual(
                title = goal.name,
                metric = MoneyFormatter.formatMinor(remaining, goal.currency),
                detail = "Remaining goal gap",
                progress = if (goal.targetAmountMinor <= 0L) 0f else goal.currentAmountMinor.toFloat() / goal.targetAmountMinor.toFloat(),
                isPositive = analytics.netCashFlowMinor > 0L,
                category = TransactionCategory.INVESTMENT,
            )
        }
        "yesterday" in normalized -> ChatVisual(
            title = "Yesterday",
            metric = MoneyFormatter.formatMinor(recent.yesterdaySpendMinor, "INR"),
            detail = "Recent daily average ${MoneyFormatter.formatMinor(recent.dailyAverageMinor, "INR")}",
            progress = if (recent.dailyAverageMinor <= 0L) 0f else recent.yesterdaySpendMinor.toFloat() / recent.dailyAverageMinor.toFloat(),
            isPositive = recent.yesterdaySpendMinor <= recent.dailyAverageMinor,
            category = category?.category,
        )
        "today" in normalized || "daily" in normalized -> ChatVisual(
            title = "Today",
            metric = MoneyFormatter.formatMinor(recent.todaySpendMinor, "INR"),
            detail = if (dailyBudget > 0L) "Daily guardrail ${MoneyFormatter.formatMinor(dailyBudget, "INR")}" else "Income needed for guardrail",
            progress = if (dailyBudget <= 0L) 0f else recent.todaySpendMinor.toFloat() / dailyBudget.toFloat(),
            isPositive = dailyBudget > 0L && recent.todaySpendMinor <= dailyBudget,
            category = category?.category,
        )
        else -> ChatVisual(
            title = category?.category?.displayName() ?: "Recent spend",
            metric = MoneyFormatter.formatMinor(recent.last7DaysSpendMinor, "INR"),
            detail = "Last 7 days",
            progress = if (analytics.totalExpenseMinor <= 0L) 0f else recent.last7DaysSpendMinor.toFloat() / analytics.totalExpenseMinor.toFloat(),
            isPositive = analytics.netCashFlowMinor >= 0L,
            category = category?.category,
        )
    }
}

private fun String.compactAssistantText(): String {
    val cleaned = substringBefore("\n\nSource:")
        .replace("\n\n", " ")
        .replace("\n", " ")
        .trim()
    return if (cleaned.length <= 220) cleaned else cleaned.take(217).trimEnd() + "..."
}

@Composable
private fun profileTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = NamiSurfaceHigh,
    unfocusedContainerColor = NamiSurfaceHigh,
    disabledContainerColor = NamiSurfaceHigh,
    focusedIndicatorColor = NamiPrimary,
    unfocusedIndicatorColor = Color.White.copy(alpha = 0.08f),
    focusedTextColor = NamiOnSurface,
    unfocusedTextColor = NamiOnSurface,
    focusedLabelColor = NamiPrimary,
    unfocusedLabelColor = NamiOutline,
)

private fun String.toRupeeMinor(): Long {
    return filter { it.isDigit() }
        .toLongOrNull()
        ?.times(100L)
        ?: 0L
}

private fun copyProfilePhoto(context: Context, uri: Uri): String? {
    val profileDir = File(context.filesDir, "profile").apply { mkdirs() }
    val destination = File(profileDir, "avatar.jpg")
    return runCatching {
        context.contentResolver.openInputStream(uri)?.use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        destination.absolutePath
    }.getOrNull()
}

@Composable
private fun SettingsScreen(context: Context, modifier: Modifier = Modifier) {
    val isNotificationAccessEnabled = remember { NotificationAccess.isEnabled(context) }

    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ActionPanel(
            title = "Notification access",
            body = if (isNotificationAccessEnabled) {
                "Enabled. SpendSense can capture supported transaction notifications."
            } else {
                "Not enabled. Turn this on to let SpendSense build your private financial timeline."
            },
            action = "Open settings",
            onAction = { context.startActivity(NotificationAccess.settingsIntent()) },
        )
        Text(
            text = "Financial data stays on this device. Raw notification text is not part of the production persistence path.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
