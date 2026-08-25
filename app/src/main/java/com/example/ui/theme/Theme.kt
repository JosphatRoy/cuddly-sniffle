package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkNaturalGreenPrimary,
    secondary = DarkNaturalText,
    tertiary = DarkNaturalSageAccent,
    background = DarkNaturalBg,
    surface = DarkNaturalCard,
    onPrimary = DarkNaturalBg,
    onSecondary = DarkNaturalBg,
    onBackground = DarkNaturalText,
    onSurface = DarkNaturalText,
    surfaceVariant = DarkNaturalItemBg,
    onSurfaceVariant = DarkNaturalText,
    outline = DarkNaturalBorder,
    outlineVariant = DarkNaturalItemBorder
)

private val LightColorScheme = lightColorScheme(
    primary = NaturalGreenPrimary,
    secondary = NaturalHighlight,
    tertiary = NaturalSageAccent,
    background = NaturalBg,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = NaturalText,
    onSurface = NaturalText,
    surfaceVariant = NaturalPromoBg,
    onSurfaceVariant = NaturalHighlight,
    outline = NaturalCardBorder,
    outlineVariant = NaturalItemBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We intentionally ignore dynamicColor to preserve our hand-crafted Warm Organic identity
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
