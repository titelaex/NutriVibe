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
    secondaryContainer = DarkSurface,
    onSecondaryContainer = MintGreen,
    tertiary = CalorieAmber,
    onTertiary = Color.Black,
    background = DarkPine,
    onBackground = Color(0xFFF2FBF4),
    surface = DarkSurface,
    onSurface = Color(0xFFF2FBF4),
    surfaceVariant = DarkCard,
    onSurfaceVariant = Color(0xFFB5C9BE),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF335741)
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = MintGreen,
    onPrimaryContainer = ForestGreen,
    secondary = OliveGreen,
    onSecondary = Color.White,
    secondaryContainer = SandCream,
    onSecondaryContainer = ForestGreen,
    tertiary = CalorieAmber,
    onTertiary = Color.White,
    background = WarmWhite,
    onBackground = Color(0xFF081C15),
    surface = Color.White,
    onSurface = Color(0xFF081C15),
    surfaceVariant = SandCream,
    onSurfaceVariant = Color(0xFF3F5A49),
    error = ErrorRed,
    onError = Color.White,
    outline = Color(0xFF759982)
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