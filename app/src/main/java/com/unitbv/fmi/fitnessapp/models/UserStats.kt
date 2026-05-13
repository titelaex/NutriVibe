package com.unitbv.fmi.fitnessapp.models

data class UserStats(
    val firstName: String = "",
    val lastName: String = "",
    val age: Int = 0,
    val gender: String = "", // "Masculin", "Feminin"
    val height: Float = 0f, // in cm
    val weight: Float = 0f, // in kg
    val activityLevel: String = "", // "Sedentar", "Ușor activ", "Moderat", "Foarte activ"
    val workoutsPerWeek: Int = 0,
    val goal: String = "", // "Slăbire", "Menținere", "Creștere masă"
    val goalRhythm: Float = 0f, // kg/week (max 1.0)
    val bodyType: String = "", // "Ectomorf", "Mezomorf", "Endomorf"
    val dietaryRestrictions: String = "",
    val allergies: String = "",
    val medicalConditions: String = "",
    
    // Calculated results
    val bmi: Float = 0f,
    val bmr: Int = 0,
    val tdee: Int = 0,
    val dailyCalories: Int = 0,
    val proteinGrams: Int = 0,
    val carbGrams: Int = 0,
    val fatGrams: Int = 0,
    val fiberGrams: Int = 0
)
