package com.unitbv.fmi.fitnessapp.logic

import com.unitbv.fmi.fitnessapp.models.UserStats
import kotlin.math.pow
import kotlin.math.roundToInt

object FitnessCalculator {

    fun calculateBMI(weight: Float, height: Float): Float {
        val heightInMeters = height / 100
        return weight / heightInMeters.pow(2)
    }

    fun determineBodyType(
        gainsWeightEasily: Boolean, // "Cât de ușor te îngrași..."
        wristSize: String, // "Subțire", "Medie", "Robustă"
        childhoodBody: String // "Slab", "Normal", "Plinuț"
    ): String {
        var endoScore = 0
        var ectoScore = 0
        
        if (gainsWeightEasily) endoScore++ else ectoScore++
        if (wristSize == "Robustă") endoScore++
        if (wristSize == "Subțire") ectoScore++
        if (childhoodBody == "Plinuț") endoScore++
        if (childhoodBody == "Slab") ectoScore++
        
        return when {
            endoScore >= 2 -> "Endomorf"
            ectoScore >= 2 -> "Ectomorf"
            else -> "Mezomorf"
        }
    }

    fun calculateMacros(stats: UserStats): UserStats {
        val bmi = calculateBMI(stats.weight, stats.height)
        
        // 1. Basic BMR calculation (Mifflin-St Jeor)
        val bmr = if (stats.gender == "Masculin") {
            (10 * stats.weight) + (6.25 * stats.height) - (5 * stats.age) + 5
        } else {
            (10 * stats.weight) + (6.25 * stats.height) - (5 * stats.age) - 161
        }
        
        // 2. PAL Multiplier
        val pal = when (stats.activityLevel) {
            "Sedentar" -> 1.2
            "Ușor activ" -> 1.375
            "Moderat" -> 1.55
            "Foarte activ" -> 1.725
            else -> 1.2
        }
        
        var tdee = bmr * pal
        
        // 3. Somatotype adjustments to TDEE
        when (stats.bodyType) {
            "Ectomorf" -> tdee *= 1.10 // +10%
            "Endomorf" -> tdee *= 0.95 // -5%
        }
        
        // 4. Adjust for Goals
        val targetCalories = when (stats.goal) {
            "Slăbire" -> tdee - 500.0 // approx 15-25%
            "Creștere masă" -> tdee * 1.15 // +15%
            else -> tdee // Menținere
        }
        
        // 5. Macro Split based on Somatotype
        val (carbPercent, proteinPercent, fatPercent) = when (stats.bodyType) {
            "Ectomorf" -> Triple(0.55, 0.25, 0.20)
            "Endomorf" -> Triple(0.25, 0.40, 0.35)
            else -> Triple(0.40, 0.30, 0.30) // Mezomorf default
        }
        
        val protein = (targetCalories * proteinPercent / 4).roundToInt()
        val carbs = (targetCalories * carbPercent / 4).roundToInt()
        val fat = (targetCalories * fatPercent / 9).roundToInt()
        
        // 6. Fiber (14g per 1000kcal)
        val fiber = ((targetCalories / 1000) * 14).roundToInt()
        
        return stats.copy(
            bmi = bmi,
            bmr = bmr.roundToInt(),
            tdee = tdee.roundToInt(),
            dailyCalories = targetCalories.roundToInt(),
            proteinGrams = protein,
            carbGrams = carbs,
            fatGrams = fat,
            fiberGrams = fiber
        )
    }
}
