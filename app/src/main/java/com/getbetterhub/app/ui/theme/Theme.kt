package com.getbetterhub.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// Get Better Hub is dark-themed only in this version, matching the approved mockups —
// there is no light variant, consistent with the design's identity (orange-on-black).
private val AppColorScheme = darkColorScheme(
    background = AppBackground,
    surface = SurfaceCard,
    primary = AccentOrange,
    onPrimary = AccentOrangeOnFill,
    secondary = AccentRed,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = AccentRed
)

@Composable
fun GetBetterHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // reserved for future light-theme support
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = AppTypography,
        content = content
    )
}
