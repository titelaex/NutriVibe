package com.unitbv.fmi.fitnessapp.models

data class Workout(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "Fitness",
    val durationMin: Int = 15,
    val difficulty: String = "Ușor"
)
