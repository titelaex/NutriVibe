package com.unitbv.fmi.fitnessapp.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = SageGreen,
    onPrimary = Color.White,
    primaryContainer = DarkCard,
    onPrimaryContainer = LightSage,
    secondary = LightSage,
    onSecondary = DarkPine,
    secondaryContainer = Color(0xFF1E3A24),
    onSecondaryContainer = MintGreen,
    tertiary = CalorieAmber,
    onTertiary = Color.Black,
    background = DarkPine,
    onBackground = Color(0xFFE8F0E8),
    surface = DarkSurface,
    onSurface = Color(0xFFE8F0E8),
    surfaceVariant = DarkCard,
    onSurfaceVariant = Color(0xFFBBC8BC),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF3D5C42)
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = MintGreen,
    onPrimaryContainer = ForestGreen,
    secondary = OliveGreen,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFC8E6C9),
    onSecondaryContainer = ForestGreen,
    tertiary = CalorieAmber,
    onTertiary = Color.White,
    background = WarmWhite,
    onBackground = Color(0xFF1A1C19),
    surface = Color.White,
    onSurface = Color(0xFF1A1C19),
    surfaceVariant = SandCream,
    onSurfaceVariant = Color(0xFF43503F),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF73867A)
)

@Composable
fun UnitBvFMI2026Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

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