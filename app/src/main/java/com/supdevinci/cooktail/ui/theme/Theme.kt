package com.supdevinci.cooktail.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Thème élégant "Cream & Forest"
private val LightColorScheme = lightColorScheme(
    primary = DeepForest,
    onPrimary = CreamBg,
    secondary = SageGreen,
    onSecondary = CreamBg,
    tertiary = SoftGold,
    onTertiary = DarkCharcoal,
    background = CreamBg,
    onBackground = DarkCharcoal,
    surface = CardIvory,
    onSurface = DarkCharcoal,
    secondaryContainer = SelectedGreen,
    onSecondaryContainer = DeepForest,
    surfaceVariant = Color(0xFFF0EAE5),
    onSurfaceVariant = MutedSlate,
    outline = SageGreen,
    error = ErrorRose,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = SoftGold,
    onPrimary = DeepForest,
    secondary = SageGreen,
    onSecondary = DeepForest,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = CreamBg,
    onSurface = CreamBg,
    outline = SageGreen
)

@Composable
fun CookTailTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CookTailTypography,
        content = content
    )
}
