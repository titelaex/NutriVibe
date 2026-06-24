package com.unitbv.fmi.fitnessapp.models

data class UserStats(
    var firstName: String = "",
    var lastName: String = "",
    var age: Int = 0,
    var gender: String = "", // "Masculin", "Feminin"
    var height: Float = 0f, // in cm
    var weight: Float = 0f, // in kg
    var activityLevel: String = "", // "Sedentar", "Ușor activ", "Moderat", "Foarte activ"
    var workoutsPerWeek: Int = 0,
    var goal: String = "", // "Slăbire", "Menținere", "Creștere masă"
    var goalRhythm: Float = 0f, // kg/week (max 1.0)
    var bodyType: String = "", // "Ectomorf", "Mezomorf", "Endomorf"
    var dietaryRestrictions: String = "",
    var allergies: String = "",
    var medicalConditions: String = "",
    
    // Calculated results
    var bmi: Float = 0f,
    var bmr: Int = 0,
    var tdee: Int = 0,
    var dailyCalories: Int = 0,
    var proteinGrams: Int = 0,
    var carbGrams: Int = 0,
    var fatGrams: Int = 0,
    var fiberGrams: Int = 0
)
