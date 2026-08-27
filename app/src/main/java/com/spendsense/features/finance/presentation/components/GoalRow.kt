package com.spendsense.features.finance.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spendsense.core.presentation.MoneyFormatter
import com.spendsense.features.finance.domain.SpendingGoal

@Composable
fun GoalRow(goal: SpendingGoal) {
    val progress = if (goal.targetAmountMinor == 0L) {
        0f
    } else {
        (goal.currentAmountMinor.toFloat() / goal.targetAmountMinor.toFloat()).coerceIn(0f, 1f)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = goal.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${(progress * 100).toInt()}% funded",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Text(
                text = MoneyFormatter.formatMinor(goal.currentAmountMinor, goal.currency) +
                    " / " +
                    MoneyFormatter.formatMinor(goal.targetAmountMinor, goal.currency),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
