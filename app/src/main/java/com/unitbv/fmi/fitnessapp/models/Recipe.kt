package com.unitbv.fmi.fitnessapp.models

data class Recipe(
    val id: String = "",
    val name: String = "",
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fats: Int = 0,
    val category: String = "Mic Dejun", // Mic Dejun, Pranz, Cina
    val ingredients: List<String> = emptyList(),
    val instructions: String = "",
    val difficulty: String = "Ușor",
    val prepTime: Int = 15
)
