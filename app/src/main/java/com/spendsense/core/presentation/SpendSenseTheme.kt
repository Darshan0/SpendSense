package com.spendsense.core.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpendSenseColors = darkColorScheme(
    primary = Color(0xFFC2C1FF),
    onPrimary = Color(0xFF1F1A5F),
    primaryContainer = Color(0xFF332DBC),
    onPrimaryContainer = Color(0xFFE2DFFF),
    secondary = Color(0xFFAAC7FF),
    onSecondary = Color(0xFF003064),
    secondaryContainer = Color(0xFF003064),
    onSecondaryContainer = Color(0xFFD6E3FF),
    tertiary = Color(0xFF64D6A3),
    background = Color(0xFF0E0E10),
    onBackground = Color(0xFFE4E2E4),
    surface = Color(0xFF1B1B1D),
    onSurface = Color(0xFFE4E2E4),
    surfaceVariant = Color(0xFF303032),
    onSurfaceVariant = Color(0xFFC8C6C8),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

@Composable
fun SpendSenseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SpendSenseColors,
        typography = MaterialTheme.typography,
        content = content,
    )
}
