package com.spendsense.features.finance.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.spendsense.core.presentation.MoneyFormatter
import com.spendsense.features.finance.domain.TransactionType

@Composable
fun AmountText(
    amountMinor: Long,
    currencyCode: String,
    type: TransactionType?,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Text(
        text = MoneyFormatter.formatMinor(
            amountMinor = amountMinor,
            currencyCode = currencyCode,
            type = type,
        ),
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        color = color,
        textAlign = TextAlign.End,
    )
}
