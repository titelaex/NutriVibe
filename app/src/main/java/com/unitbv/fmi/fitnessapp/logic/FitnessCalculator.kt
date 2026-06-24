package com.unitbv.fmi.fitnessapp.logic

import com.unitbv.fmi.fitnessapp.models.UserStats
import kotlin.math.pow
import kotlin.math.roundToInt

object FitnessCalculator {

    fun calculateBMI(weight: Float, height: Float): Float {
        val heightInMeters = height / 100
        return weight / heightInMeters.pow(2)
    }

    fun calculateMacros(stats: UserStats): UserStats {
        val bmi = calculateBMI(stats.weight, stats.height)
        
        // BMR: nr de kcal pe care corpul le arde/zi pt a ramane in viata, fara ex
        val bmr = if (stats.gender == "Masculin") {
            (10 * stats.weight) + (6.25 * stats.height) - (5 * stats.age) + 5
        } else {
            (10 * stats.weight) + (6.25 * stats.height) - (5 * stats.age) - 161
        }
        
        // 2. PAL Multiplier: niv activ fizica
        val pal = when (stats.activityLevel) {
            "Sedentar" -> 1.2
            "Ușor activ" -> 1.375
            "Moderat" -> 1.55
            "Foarte activ" -> 1.725
            else -> 1.2
        }
        
        var tdee = bmr * pal // cal consumate/zi
        
        // Somatotype adjustments to TDEE
        when (stats.bodyType) {
            "Ectomorf" -> tdee *= 1.10 // +10%
            "Endomorf" -> tdee *= 0.95 // -5%
        }
        
        // Adjust for Goals
        val targetCalories = when (stats.goal) {
            "Slăbire" -> tdee - 500.0 // approx 15-25%
            "Creștere masă" -> tdee * 1.15 // +15%
            else -> tdee // Menținere
        }
        
        // Macro Split based on Somatotype
        val (carbPercent, proteinPercent, fatPercent) = when (stats.bodyType) {
            "Ectomorf" -> Triple(0.55, 0.25, 0.20)
            "Endomorf" -> Triple(0.25, 0.40, 0.35)
            else -> Triple(0.40, 0.30, 0.30) // Mezomorf default
        }
        
        val protein = (targetCalories * proteinPercent / 4).roundToInt()
        val carbs = (targetCalories * carbPercent / 4).roundToInt()
        val fat = (targetCalories * fatPercent / 9).roundToInt()
        
        // Fiber (14g/1000kcal)
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
