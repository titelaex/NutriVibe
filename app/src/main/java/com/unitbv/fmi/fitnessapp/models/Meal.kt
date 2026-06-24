package com.unitbv.fmi.fitnessapp.models

import com.google.firebase.Timestamp

data class Meal(
    var id: String = "",
    var name: String = "",
    var grams: Int = 0,
    var calories: Int = 0,
    var protein: Int = 0,
    var carbs: Int = 0,
    var fats: Int = 0,
    var type: String = "Mic Dejun",
    var timestamp: Timestamp = Timestamp.now()
)
