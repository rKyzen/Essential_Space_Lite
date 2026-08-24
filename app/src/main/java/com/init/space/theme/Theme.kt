package com.init.space.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = SignalAccent,
    onPrimary = DarkBackground,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = DarkTextPrimary,
    secondary = DarkTextSecondary,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = SignalAccentDim,
    onTertiary = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorderSubtle,
    error = SignalMutedAmber,
    onError = DarkBackground,
    errorContainer = DarkSurfaceElevated,
    onErrorContainer = DarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = SignalAccentInverted,
    onPrimary = LightBackground,
    primaryContainer = LightSurfaceElevated,
    onPrimaryContainer = LightTextPrimary,
    secondary = LightTextSecondary,
    onSecondary = LightBackground,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightTextPrimary,
    tertiary = SignalAccentDim,
    onTertiary = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorderSubtle,
    error = SignalMutedAmber,
    onError = LightBackground,
    errorContainer = LightSurfaceElevated,
    onErrorContainer = LightTextPrimary
)

// Refined squircle shapes matching _init_ /files
val InitShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

val ColorScheme.surfaceElevated: Color
    get() = this.primaryContainer

@Composable
fun InitTheme(
    darkTheme: Boolean = true, // Dark-first by default in _init_ family
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = InitTypography,
        shapes = InitShapes,
        content = content
    )
}
