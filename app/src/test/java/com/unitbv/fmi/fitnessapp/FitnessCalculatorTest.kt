package com.unitbv.fmi.fitnessapp

import com.unitbv.fmi.fitnessapp.logic.FitnessCalculator
import com.unitbv.fmi.fitnessapp.models.UserStats
import org.junit.Assert.*
import org.junit.Test

class FitnessCalculatorTest {

    @Test
    fun testCalculateBMI() {
        val height = 180f // 1.8m
        val weight = 80f
        // BMI = 80 / 1.8^2 = 80 / 3.24 = 24.6913...
        val expectedBmi = 24.691358f
        val actualBmi = FitnessCalculator.calculateBMI(weight, height)
        assertEquals(expectedBmi, actualBmi, 0.01f)
    }

    @Test
    fun testDetermineBodyType_Ectomorph() {
        // Not gaining weight easily, thin wrist, thin childhood
        val bodyType = FitnessCalculator.determineBodyType(
            gainsWeightEasily = false,
            wristSize = "Subțire",
            childhoodBody = "Slab"
        )
        assertEquals("Ectomorf", bodyType)
    }

    @Test
    fun testDetermineBodyType_Endomorph() {
        // Gaining weight easily, robust wrist, chubby childhood
        val bodyType = FitnessCalculator.determineBodyType(
            gainsWeightEasily = true,
            wristSize = "Robustă",
            childhoodBody = "Plinuț"
        )
        assertEquals("Endomorf", bodyType)
    }

    @Test
    fun testDetermineBodyType_Mesomorph() {
        // Normal gain, medium wrist, normal childhood
        val bodyType = FitnessCalculator.determineBodyType(
            gainsWeightEasily = true,
            wristSize = "Medie",
            childhoodBody = "Normal"
        )
        assertEquals("Mezomorf", bodyType)
    }

    @Test
    fun testCalculateMacros_Male_Ectomorph() {
        val stats = UserStats(
            firstName = "John",
            lastName = "Doe",
            age = 25,
            gender = "Masculin",
            height = 180f,
            weight = 70f,
            activityLevel = "Sedentar",
            bodyType = "Ectomorf",
            goal = "Creștere masă"
        )

        // BMR = (10 * 70) + (6.25 * 180) - (5 * 25) + 5 = 700 + 1125 - 125 + 5 = 1705
        // PAL = 1.2
        // TDEE = 1705 * 1.2 = 2046
        // Ectomorph TDEE Adjustment = 2046 * 1.10 = 2250.6
        // Goal "Creștere masă" Calories = 2250.6 * 1.15 = 2588.19 -> ~2588 kcal
        // Ectomorph split: Carbs 55%, Protein 25%, Fat 20%
        // Protein = 2588 * 0.25 / 4 = 161.75g -> 162g
        // Carbs = 2588 * 0.55 / 4 = 355.85g -> 356g
        // Fat = 2588 * 0.20 / 9 = 57.51g -> 58g
        // Fiber = (2588 / 1000) * 14 = 36.23 -> 36g

        val result = FitnessCalculator.calculateMacros(stats)

        assertEquals(21.6f, result.bmi, 0.1f)
        assertEquals(1705, result.bmr)
        assertEquals(2251, result.tdee)
        assertEquals(2588, result.dailyCalories)
        assertEquals(162, result.proteinGrams)
        assertEquals(356, result.carbGrams)
        assertEquals(58, result.fatGrams)
        assertEquals(36, result.fiberGrams)
    }

    @Test
    fun testCalculateMacros_Female_Endomorph_WeightLoss() {
        val stats = UserStats(
            firstName = "Jane",
            lastName = "Doe",
            age = 30,
            gender = "Feminin",
            height = 160f,
            weight = 80f,
            activityLevel = "Moderat",
            bodyType = "Endomorf",
            goal = "Slăbire"
        )

        // BMR = (10 * 80) + (6.25 * 160) - (5 * 30) - 161 = 800 + 1000 - 150 - 161 = 1489
        // PAL = 1.55
        // TDEE = 1489 * 1.55 = 2307.95
        // Endomorph TDEE Adjustment = 2307.95 * 0.95 = 2192.5525
        // Goal "Slăbire" Calories = 2192.5525 - 500 = 1692.5525 -> ~1693 kcal
        // Endomorph split: Carbs 25%, Protein 40%, Fat 35%
        // Protein = 1693 * 0.40 / 4 = 169.3g -> 169g
        // Carbs = 1693 * 0.25 / 4 = 105.81g -> 106g
        // Fat = 1693 * 0.35 / 9 = 65.83g -> 66g
        // Fiber = (1693 / 1000) * 14 = 23.7 -> 24g

        val result = FitnessCalculator.calculateMacros(stats)

        assertEquals(31.25f, result.bmi, 0.1f)
        assertEquals(1489, result.bmr)
        assertEquals(2193, result.tdee)
        assertEquals(1693, result.dailyCalories)
        assertEquals(169, result.proteinGrams)
        assertEquals(106, result.carbGrams)
        assertEquals(66, result.fatGrams)
        assertEquals(24, result.fiberGrams)
    }
}
