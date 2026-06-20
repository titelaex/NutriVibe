package com.unitbv.fmi.fitnessapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    data object Login : Screen("login", "Login")
    data object Onboarding : Screen("onboarding", "Onboarding")
    data object Dashboard : Screen("dashboard", "Acasa", Icons.Filled.Home, Icons.Outlined.Home)
    data object Recipes : Screen("recipes", "Retete", Icons.Filled.Restaurant, Icons.Outlined.Restaurant)
    data object Profile : Screen("profile", "Profil", Icons.Filled.Person, Icons.Outlined.Person)
}

val bottomNavItems = listOf(Screen.Dashboard, Screen.Recipes, Screen.Profile)
