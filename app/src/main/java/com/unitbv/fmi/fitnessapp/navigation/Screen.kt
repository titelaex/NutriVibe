package com.unitbv.fmi.fitnessapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Login : Screen("login", "Autentificare")
    object Onboarding : Screen("onboarding", "Profil Nou")
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Recipes : Screen("recipes", "Rețete", Icons.Default.RestaurantMenu)
    object Profile : Screen("profile", "Profil", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Recipes,
    Screen.Profile
)
