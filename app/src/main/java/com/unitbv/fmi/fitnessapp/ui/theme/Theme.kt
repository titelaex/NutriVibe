package com.unitbv.fmi.fitnessapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SageGreen,
    secondary = LightSage,
    tertiary = OliveGreen,
    background = DarkPine,
    surface = Color(0xFF1E3529),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = WarmWhite,
    onSurface = WarmWhite
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreen,
    secondary = OliveGreen,
    tertiary = SageGreen,
    background = WarmWhite,
    surface = SandCream,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF132B1F),
    onSurface = Color(0xFF132B1F)
)

@Composable
fun UnitBvFMI2026Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to preserve our premium custom organic color scheme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}