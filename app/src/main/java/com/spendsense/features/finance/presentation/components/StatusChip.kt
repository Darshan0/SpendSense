package com.spendsense.features.finance.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spendsense.features.finance.domain.VerificationStatus

@Composable
fun StatusChip(status: VerificationStatus) {
    val label = when (status) {
        VerificationStatus.AUTO_VERIFIED -> "Auto"
        VerificationStatus.NEEDS_REVIEW -> "Review"
        VerificationStatus.USER_VERIFIED -> "Verified"
    }

    Text(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                shape = RoundedCornerShape(6.dp),
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
    )
}
