package com.unitbv.fmi.fitnessapp.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.unitbv.fmi.fitnessapp.data.FirebaseService
import com.unitbv.fmi.fitnessapp.models.UserStats
import com.unitbv.fmi.fitnessapp.logic.FitnessCalculator
import kotlinx.coroutines.launch
import com.unitbv.fmi.fitnessapp.ui.theme.*

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var currentStep by remember { mutableIntStateOf(1) }
    
    // A. Biometrics
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Bărbat") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    
    // B. Lifestyle
    var activityLevel by remember { mutableStateOf("Sedentar") }
    
    // C. Goals
    var goal by remember { mutableStateOf("Menținere") }
    var targetWeight by remember { mutableStateOf("") }
    var targetTimeframeWeeks by remember { mutableFloatStateOf(4f) }
    
    // D. Somatotype
    var bodyType by remember { mutableStateOf("Ectomorf") }
    
    // E. Medical
    var medicalConditions by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val progress = currentStep / 5f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Personalizează Experiența", 
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Completează datele pentru planul tău personalizat",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Progress Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = ForestGreen,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pasul $currentStep din 5",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Conditionally render steps
        when (currentStep) {
            1 -> {
                // Section A: Date Biometrice
                SectionCard(title = "A. Date Biometrice", icon = Icons.Rounded.Straighten) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Surface(
                            modifier = Modifier.weight(1f).clickable { gender = "Bărbat" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (gender == "Bărbat") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (gender == "Bărbat") BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Rounded.Male, contentDescription = null, tint = if (gender == "Bărbat") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Bărbat", color = if (gender == "Bărbat") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        }
                        Surface(
                            modifier = Modifier.weight(1f).clickable { gender = "Femeie" },
                            shape = RoundedCornerShape(12.dp),
                            color = if (gender == "Femeie") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (gender == "Femeie") BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                Icon(Icons.Rounded.Female, contentDescription = null, tint = if (gender == "Femeie") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Femeie", color = if (gender == "Femeie") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = age, onValueChange = { age = it }, label = { Text("Vârsta (ani)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = height, onValueChange = { height = it }, label = { Text("Înălțime (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = weight, onValueChange = { weight = it }, label = { Text("Greutate (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
            2 -> {
                // Section B: Stil de Viață
                SectionCard(title = "B. Stil de Viață", icon = Icons.Rounded.DirectionsRun) {
                    Text("Nivel de activitate fizică:", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            listOf("Sedentar" to "Muncă de birou, fără sport", 
                                   "Ușor activ" to "1-3 antrenamente/săptămână", 
                                   "Moderat" to "3-5 antrenamente/săptămână", 
                                   "Foarte activ" to "Sport zilnic").forEach { (level, desc) ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { activityLevel = level }.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    RadioButton(selected = activityLevel == level, onClick = { activityLevel = level }, colors = RadioButtonDefaults.colors(selectedColor = ForestGreen))
                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Text(level, fontWeight = FontWeight.Bold)
                                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // Section C: Obiective
                SectionCard(title = "C. Obiective", icon = Icons.Rounded.Flag) {
                    Text("Care este scopul tău principal?", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            listOf("Slăbire" to "Slăbire", "Menținere" to "Menținere", "Creștere masă" to "Creștere masă").forEach { (gVal, gLabel) ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { goal = gVal }.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    RadioButton(selected = goal == gVal, onClick = { goal = gVal }, colors = RadioButtonDefaults.colors(selectedColor = ForestGreen))
                                    Text(gLabel, modifier = Modifier.padding(start = 8.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    if (goal != "Menținere") {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = targetWeight, onValueChange = { targetWeight = it }, label = { Text("Greutate dorită (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("În cât timp? (aprox. ${targetTimeframeWeeks.toInt()} săptămâni)")
                        Slider(
                            value = targetTimeframeWeeks, onValueChange = { targetTimeframeWeeks = it },
                            valueRange = 4f..52f, steps = 48,
                            colors = SliderDefaults.colors(thumbColor = ForestGreen, activeTrackColor = ForestGreen)
                        )
                    }
                }
            }
            4 -> {
                // Section D: Somatotip
                SectionCard(title = "D. Evaluare Corp", icon = Icons.Rounded.Accessibility) {
                    Text("Alege tipul tău corporal:", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            listOf("Ectomorf" to "Subțire, greu de pus masă", 
                                   "Mezomorf" to "Atlet, pune masă ușor", 
                                   "Endomorf" to "Structură robustă, stochează grăsime ușor").forEach { (type, desc) ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { bodyType = type }.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                    RadioButton(selected = bodyType == type, onClick = { bodyType = type }, colors = RadioButtonDefaults.colors(selectedColor = ForestGreen))
                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Text(type, fontWeight = FontWeight.Bold)
                                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            5 -> {
                // Section E: Medical (Opțional)
                SectionCard(title = "E. Profil Medical (Opțional)", icon = Icons.Rounded.LocalHospital) {
                    OutlinedTextField(
                        value = medicalConditions, onValueChange = { medicalConditions = it }, 
                        label = { Text("Alergii sau afecțiuni (ex: diabet, alergie nuci)") },
                        modifier = Modifier.fillMaxWidth(), minLines = 2
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentStep > 1) {
                OutlinedButton(
                    onClick = {
                        errorMessage = null
                        currentStep--
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen),
                    border = BorderStroke(1.dp, ForestGreen)
                ) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Înapoi", style = MaterialTheme.typography.titleMedium)
                }
            }

            Button(
                onClick = {
                    errorMessage = null
                    
                    // Validate current step before proceeding or finishing
                    if (currentStep == 1) {
                        val ageVal = age.toIntOrNull()
                        val heightVal = height.toFloatOrNull()
                        val weightVal = weight.toFloatOrNull()

                        if (ageVal == null || ageVal !in 10..120) {
                            errorMessage = "Te rugăm să introduci o vârstă validă (între 10 și 120 de ani)."
                            return@Button
                        }
                        if (heightVal == null || heightVal !in 100f..250f) {
                            errorMessage = "Te rugăm să introduci o înălțime validă (între 100 și 250 cm)."
                            return@Button
                        }
                        if (weightVal == null || weightVal !in 30f..300f) {
                            errorMessage = "Te rugăm să introduci o greutate validă (între 30 și 300 kg)."
                            return@Button
                        }
                        currentStep = 2
                    } else if (currentStep == 2) {
                        currentStep = 3
                    } else if (currentStep == 3) {
                        val initialWeightVal = weight.toFloatOrNull() ?: 0f
                        if (goal != "Menținere") {
                            val tWeightVal = targetWeight.toFloatOrNull()
                            if (tWeightVal == null || tWeightVal !in 30f..300f) {
                                errorMessage = "Te rugăm să introduci o greutate dorită validă (între 30 și 300 kg)."
                                return@Button
                            }
                            if (goal == "Slăbire" && tWeightVal >= initialWeightVal) {
                                errorMessage = "Pentru slăbire, greutatea dorită trebuie să fie mai mică decât greutatea actuală ($initialWeightVal kg)."
                                return@Button
                            }
                            if (goal == "Creștere masă" && tWeightVal <= initialWeightVal) {
                                errorMessage = "Pentru creștere, greutatea dorită trebuie să fie mai mare decât greutatea actuală ($initialWeightVal kg)."
                                return@Button
                            }
                        }
                        currentStep = 4
                    } else if (currentStep == 4) {
                        currentStep = 5
                    } else if (currentStep == 5) {
                        // Submit logic (finish onboarding)
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            try {
                                val a = age.toInt()
                                val h = height.toFloat()
                                val w = weight.toFloat()
                                
                                val userProfile = FirebaseAuth.getInstance().currentUser
                                val fName = userProfile?.displayName?.split(" ")?.firstOrNull() ?: ""
                                val lName = userProfile?.displayName?.split(" ")?.drop(1)?.joinToString(" ") ?: ""

                                val baseStats = UserStats(
                                    firstName = fName,
                                    lastName = lName,
                                    age = a,
                                    gender = gender,
                                    weight = w,
                                    height = h,
                                    activityLevel = activityLevel,
                                    goal = goal,
                                    bodyType = bodyType,
                                    medicalConditions = medicalConditions
                                )
                                
                                val finalStats = FitnessCalculator.calculateMacros(baseStats)
                                
                                FirebaseService.saveUserProfile(finalStats)
                                
                                val userId = userProfile?.uid
                                if (userId != null) {
                                    val sharedPrefs = context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE)
                                    sharedPrefs.edit().putBoolean("has_completed_onboarding_$userId", true).apply()
                                }
                                
                                onFinish()
                            } catch (e: Exception) {
                                errorMessage = "Verifică datele introduse. ${e.message}"
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    if (currentStep == 5) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creează Planul", style = MaterialTheme.typography.titleMedium)
                    } else {
                        Text("Următor", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Rounded.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SectionCard(title: String, icon: ImageVector, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
