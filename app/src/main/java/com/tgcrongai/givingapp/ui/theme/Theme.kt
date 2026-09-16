package com.tgcrongai.givingapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val GivingAppColorScheme = lightColorScheme(
    primary = Teal600,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = Gold,
    background = Paper,
    surface = androidx.compose.ui.graphics.Color.White,
    onBackground = Ink,
    onSurface = Ink,
    error = Red
)

@Composable
fun GivingAppTheme(content: @Composable () -> Unit) {
    // Dark theme intentionally mirrors light theme for now — the brand is a
    // single teal identity; revisit if a dark variant is requested.
    MaterialTheme(
        colorScheme = GivingAppColorScheme,
        typography = GivingAppTypography,
        content = content
    )
}
