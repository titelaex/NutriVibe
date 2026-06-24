package com.unitbv.fmi.fitnessapp.models

import com.google.firebase.Timestamp

data class Meal(
    val id: String = "",
    val name: String = "",
    val grams: Int = 0,
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fats: Int = 0,
    val type: String = "Mic Dejun", // Mic Dejun, Pranz, Cina, Gustare
    val timestamp: Timestamp = Timestamp.now()
)
