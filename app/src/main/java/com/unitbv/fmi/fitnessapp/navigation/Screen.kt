package com.unitbv.fmi.fitnessapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    data object Login : Screen("login", "Login")
    data object Onboarding : Screen("onboarding", "Onboarding")
    data object Dashboard : Screen("dashboard", "Acasă", Icons.Filled.Home, Icons.Outlined.Home)
    data object Recipes : Screen("recipes", "Rețete", Icons.Filled.Restaurant, Icons.Outlined.Restaurant)
    data object Workouts : Screen("workouts", "Exerciții", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter)
    data object Profile : Screen("profile", "Profil", Icons.Filled.Person, Icons.Outlined.Person)
}

val bottomNavItems = listOf(Screen.Dashboard, Screen.Recipes, Screen.Workouts, Screen.Profile)
