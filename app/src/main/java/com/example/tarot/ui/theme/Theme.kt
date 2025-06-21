package com.example.tarot.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MysticDarkColorScheme = darkColorScheme(
    primary = MysticGold,
    onPrimary = MysticDarkBlue,
    primaryContainer = MysticPurple,
    onPrimaryContainer = MysticLightGold,
    secondary = MysticSilver,
    onSecondary = MysticDarkBlue,
    secondaryContainer = MysticCosmic,
    onSecondaryContainer = TextSecondary,
    tertiary = MysticLightGold,
    onTertiary = MysticDarkBlue,
    background = MysticDarkBlue,
    onBackground = TextPrimary,
    surface = MysticNavy,
    onSurface = TextPrimary,
    surfaceVariant = CardBack,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder,
    error = ErrorRed,
    onError = TextPrimary
)

private val MysticLightColorScheme = lightColorScheme(
    primary = MysticPurple,
    onPrimary = MysticLightGold,
    primaryContainer = MysticLightGold,
    onPrimaryContainer = MysticDarkBlue,
    secondary = MysticCosmic,
    onSecondary = MysticLightGold,
    secondaryContainer = MysticSilver,
    onSecondaryContainer = MysticDarkBlue,
    tertiary = MysticGold,
    onTertiary = MysticDarkBlue,
    background = MysticLightGold,
    onBackground = MysticDarkBlue,
    surface = TextPrimary,
    onSurface = MysticDarkBlue,
    surfaceVariant = MysticSilver,
    onSurfaceVariant = MysticPurple,
    outline = MysticGold,
    error = ErrorRed,
    onError = TextPrimary
)

@Composable
fun TarotTheme(
    darkTheme: Boolean = true, // Default to dark theme for mystical experience
    dynamicColor: Boolean = false, // Disable dynamic colors to maintain mystical theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MysticDarkColorScheme else MysticLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
