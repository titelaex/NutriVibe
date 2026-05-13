package com.unitbv.fmi.fitnessapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction

import androidx.compose.runtime.rememberCoroutineScope
import com.unitbv.fmi.fitnessapp.logic.FitnessCalculator
import com.unitbv.fmi.fitnessapp.models.UserStats
import com.unitbv.fmi.fitnessapp.data.FirebaseService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    // Biometrics
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Masculin") }

    // Lifestyle
    var activityLevel by remember { mutableStateOf("Sedentar") }
    var workoutsPerWeek by remember { mutableStateOf("") }

    // Goals
    var goal by remember { mutableStateOf("Menținere") }
    var goalRhythm by remember { mutableStateOf(0.5f) }

    // Somatotype
    var gainsWeightEasily by remember { mutableStateOf(true) }
    var wristSize by remember { mutableStateOf("Medie") }
    var childhoodBody by remember { mutableStateOf("Normal") }

    // Medical (Optional)
    var dietaryRestrictions by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var medicalConditions by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Personalizează Experiența",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Section A: Biometrice
        Text("A. Date Biometrice", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Înălțime (cm)") },
                modifier = Modifier.weight(1f).padding(end = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                enabled = !isLoading
            )
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Greutate (kg)") },
                modifier = Modifier.weight(1f).padding(start = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                enabled = !isLoading
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Vârstă") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            enabled = !isLoading
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("Genul Biologic", modifier = Modifier.align(Alignment.Start))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = gender == "Masculin", onClick = { gender = "Masculin" }, enabled = !isLoading)
            Text("Masculin")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = gender == "Feminin", onClick = { gender = "Feminin" }, enabled = !isLoading)
            Text("Feminin")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section B: Lifestyle
        Text("B. Stil de Viață", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))

        var expandedActivity by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedActivity,
            onExpandedChange = { expandedActivity = !expandedActivity },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = activityLevel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Nivel de Activitate Zilnică") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                enabled = !isLoading
            )
            ExposedDropdownMenu(
                expanded = expandedActivity,
                onDismissRequest = { expandedActivity = false }
            ) {
                listOf("Sedentar", "Ușor activ", "Moderat", "Foarte activ").forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            activityLevel = selectionOption
                            expandedActivity = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = workoutsPerWeek,
            onValueChange = { workoutsPerWeek = it },
            label = { Text("Antrenamente pe Săptămână (0-7)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section C: Goals
        Text("C. Obiective", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        
        val goalsList = listOf("Slăbire", "Menținere", "Creștere masă")
        var expandedGoal by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedGoal,
            onExpandedChange = { expandedGoal = !expandedGoal },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = goal,
                onValueChange = {},
                readOnly = true,
                label = { Text("Obiectiv Principal") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                enabled = !isLoading
            )
            ExposedDropdownMenu(
                expanded = expandedGoal,
                onDismissRequest = { expandedGoal = false }
            ) {
                goalsList.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            goal = selectionOption
                            expandedGoal = false
                        }
                    )
                }
            }
        }

        if (goal == "Slăbire") {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ritm dorit: ${String.format("%.1f", goalRhythm)} kg/săpt", modifier = Modifier.align(Alignment.Start))
            Slider(
                value = goalRhythm,
                onValueChange = { goalRhythm = it },
                valueRange = 0.1f..1.0f,
                steps = 9,
                enabled = !isLoading
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section D: Somatotype
        Text("D. Evaluare Corp (Somatotip)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("Te îngrași ușor (din alimente procesate)?", modifier = Modifier.align(Alignment.Start))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = gainsWeightEasily, onClick = { gainsWeightEasily = true }, enabled = !isLoading)
            Text("Repede")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = !gainsWeightEasily, onClick = { gainsWeightEasily = false }, enabled = !isLoading)
            Text("Greu")
        }

        Spacer(modifier = Modifier.height(8.dp))
        var expandedWrist by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedWrist,
            onExpandedChange = { expandedWrist = !expandedWrist },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = wristSize,
                onValueChange = {},
                readOnly = true,
                label = { Text("Încheietura Mâinii") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                enabled = !isLoading
            )
            ExposedDropdownMenu(
                expanded = expandedWrist,
                onDismissRequest = { expandedWrist = false }
            ) {
                listOf("Subțire", "Medie", "Robustă").forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            wristSize = selectionOption
                            expandedWrist = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        var expandedChild by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expandedChild,
            onExpandedChange = { expandedChild = !expandedChild },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = childhoodBody,
                onValueChange = {},
                readOnly = true,
                label = { Text("Aspect în Copilărie") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                enabled = !isLoading
            )
            ExposedDropdownMenu(
                expanded = expandedChild,
                onDismissRequest = { expandedChild = false }
            ) {
                listOf("Slab", "Normal", "Plinuț").forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            childhoodBody = selectionOption
                            expandedChild = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section E: Medical
        Text("E. Profil Medical & Preferințe", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = dietaryRestrictions,
            onValueChange = { dietaryRestrictions = it },
            label = { Text("Restricții (ex: Vegan, Keto)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = allergies,
            onValueChange = { allergies = it },
            label = { Text("Alergii") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = medicalConditions,
            onValueChange = { medicalConditions = it },
            label = { Text("Afecțiuni (ex: Diabet)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                val h = height.toFloatOrNull() ?: 170f
                val w = weight.toFloatOrNull() ?: 70f
                val a = age.toIntOrNull() ?: 25
                val wPerWeek = workoutsPerWeek.toIntOrNull() ?: 0
                
                val calculatedBodyType = FitnessCalculator.determineBodyType(
                    gainsWeightEasily = gainsWeightEasily,
                    wristSize = wristSize,
                    childhoodBody = childhoodBody
                )
                
                var stats = UserStats(
                    firstName = "", 
                    lastName = "",
                    age = a,
                    gender = gender,
                    height = h,
                    weight = w,
                    activityLevel = activityLevel,
                    workoutsPerWeek = wPerWeek,
                    goal = goal,
                    goalRhythm = goalRhythm,
                    bodyType = calculatedBodyType,
                    dietaryRestrictions = dietaryRestrictions,
                    allergies = allergies,
                    medicalConditions = medicalConditions
                )
                
                stats = FitnessCalculator.calculateMacros(stats)

                scope.launch {
                    isLoading = true
                    errorMessage = null
                    android.util.Log.d("Onboarding", "Starting profile save...")
                    try {
                        FirebaseService.saveUserProfile(stats)
                        
                        val prefs = context.getSharedPreferences("fitness_prefs", android.content.Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("isFirstTimeUser", false).apply()
                        
                        android.util.Log.d("Onboarding", "Save complete, navigating...")
                        onFinish()
                    } catch (e: Exception) {
                        android.util.Log.e("Onboarding", "Save failed: ${e.message}")
                        errorMessage = e.localizedMessage ?: "A apărut o eroare la salvare"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && height.isNotEmpty() && weight.isNotEmpty() && age.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Creează Planul Personalizat")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
