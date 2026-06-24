package com.unitbv.fmi.fitnessapp.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unitbv.fmi.fitnessapp.data.FirebaseService
import com.unitbv.fmi.fitnessapp.models.UserStats
import kotlinx.coroutines.launch
import com.unitbv.fmi.fitnessapp.ui.theme.*

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var userStats by remember { mutableStateOf<UserStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }

    var metricUnits by remember {
        mutableStateOf(sharedPrefs.getString("unit_system", "Metric (kg, cm)") ?: "Metric (kg, cm)")
    }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    try {
                        userStats = FirebaseService.getUserProfile()
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileScreen", "Failed to load profile", e)
                    } finally {
                        isLoading = false
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ForestGreen)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Profilul Meu", 
                    style = MaterialTheme.typography.headlineMedium, 
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {
                    showEditDialog = true
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editează", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // User Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val initials = if (userStats != null && !userStats?.firstName.isNullOrBlank()) {
                        val f = userStats?.firstName?.firstOrNull()?.uppercase() ?: ""
                        val l = userStats?.lastName?.firstOrNull()?.uppercase() ?: ""
                        "$f$l"
                    } else {
                        "U"
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(GradientGreenStart, GradientGreenEnd))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        val fullName = if (userStats != null && !userStats?.firstName.isNullOrBlank()) {
                            "${userStats?.firstName} ${userStats?.lastName}"
                        } else {
                            "Utilizator Nou"
                        }
                        Text(fullName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(userStats?.gender ?: "Gen nespecificat", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val streak = userStats?.calorieStreak ?: 0
                            if (streak > 0) {
                                val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
                                val isTodayGoalMet = userStats?.lastStreakDate == todayStr
                                Text(" • ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Icon(
                                    imageVector = Icons.Rounded.Eco,
                                    contentDescription = null,
                                    tint = if (isTodayGoalMet) SuccessGreen else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "$streak " + if (streak == 1) "zi" else "zile",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isTodayGoalMet) SuccessGreen else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Body Analysis
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Analytics, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analiză Corporală", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val isImperial = metricUnits.startsWith("Imperial")
                
                val weightVal = userStats?.weight ?: 0f
                val heightVal = userStats?.height ?: 0f
                
                val weightStr = if (isImperial) {
                    String.format("%.1f lbs", weightVal * 2.20462f)
                } else {
                    "$weightVal kg"
                }
                
                val heightStr = if (isImperial) {
                    String.format("%.1f in", heightVal * 0.393701f)
                } else {
                    "$heightVal cm"
                }

                InfoCard(Modifier.weight(1f), "Greutate", weightStr, Icons.Rounded.MonitorWeight, CalorieAmber)
                InfoCard(Modifier.weight(1f), "Înălțime", heightStr, Icons.Rounded.Height, CarbsTeal)
                InfoCard(Modifier.weight(1f), "BMI", String.format("%.1f", userStats?.bmi ?: 0f), Icons.Rounded.Speed, ProteinCoral)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    ListItem(
                        headlineContent = { Text("Obiectiv", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(userStats?.goal ?: "Menținere") },
                        leadingContent = { Icon(Icons.Rounded.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Tip corporal", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(userStats?.bodyType ?: "Mezomorf") },
                        leadingContent = { Icon(Icons.Rounded.Accessibility, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    ListItem(
                        headlineContent = { Text("Activitate", fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(userStats?.activityLevel ?: "Moderat") },
                        leadingContent = { Icon(Icons.Rounded.DirectionsRun, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Daily Targets
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.TrackChanges, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Obiective Zilnice", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TargetCard(Modifier.weight(1f), "Calorii", "${userStats?.dailyCalories ?: 0} kcal", Icons.Rounded.LocalFireDepartment, CalorieAmber)
                    TargetCard(Modifier.weight(1f), "Proteine", "${userStats?.proteinGrams ?: 0}g", Icons.Rounded.FitnessCenter, ProteinCoral)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TargetCard(Modifier.weight(1f), "Carbohidrați", "${userStats?.carbGrams ?: 0}g", Icons.Rounded.Grain, CarbsTeal)
                    TargetCard(Modifier.weight(1f), "Grăsimi", "${userStats?.fatGrams ?: 0}g", Icons.Rounded.WaterDrop, FatsOrange)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Settings
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Setări", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    var expandedUnits by remember { mutableStateOf(false) }
                    Box {
                        ListItem(
                            headlineContent = { Text("Unități de măsură") },
                            supportingContent = { Text(metricUnits) },
                            leadingContent = { Icon(Icons.Rounded.Straighten, contentDescription = null) },
                            modifier = Modifier.clickable { expandedUnits = true },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                        DropdownMenu(
                            expanded = expandedUnits,
                            onDismissRequest = { expandedUnits = false }
                        ) {
                            listOf("Metric (kg, cm)", "Imperial (lbs, inches)").forEach { system ->
                                DropdownMenuItem(
                                    text = { Text(system) },
                                    onClick = {
                                        metricUnits = system
                                        sharedPrefs.edit().putString("unit_system", system).apply()
                                        expandedUnits = false
                                    }
                                )
                            }
                        }
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
                    ListItem(
                        headlineContent = { Text("Schimbă parola", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Trimite link de resetare pe email") },
                        leadingContent = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable {
                            val userEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email
                            if (!userEmail.isNullOrBlank()) {
                                com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(userEmail)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            android.widget.Toast.makeText(context, "Link trimis! Verifică email-ul: $userEmail", android.widget.Toast.LENGTH_LONG).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Eroare: ${task.exception?.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    }
                            } else {
                                android.widget.Toast.makeText(context, "Nu ești logat cu un email valid.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedButton(
                onClick = { onLogout() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ErrorRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
            ) {
                Icon(Icons.Rounded.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Deconectare", style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            userStats = userStats ?: UserStats(),
            onDismiss = { showEditDialog = false },
            onSave = { updatedStats ->
                scope.launch {
                    try {
                        FirebaseService.saveUserProfile(updatedStats)
                        userStats = updatedStats
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileScreen", "Failed to update profile", e)
                    } finally {
                        showEditDialog = false
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    userStats: UserStats,
    onDismiss: () -> Unit,
    onSave: (UserStats) -> Unit
) {
    var firstName by remember { mutableStateOf(userStats.firstName) }
    var lastName by remember { mutableStateOf(userStats.lastName) }
    var age by remember { mutableStateOf(if (userStats.age > 0) userStats.age.toString() else "") }
    var height by remember { mutableStateOf(if (userStats.height > 0f) userStats.height.toString() else "") }
    var weight by remember { mutableStateOf(if (userStats.weight > 0f) userStats.weight.toString() else "") }
    var gender by remember { mutableStateOf(userStats.gender.ifBlank { "Feminin" }) }
    var activityLevel by remember { mutableStateOf(userStats.activityLevel.ifBlank { "Moderat" }) }
    var goal by remember { mutableStateOf(userStats.goal.ifBlank { "Menținere" }) }
    var bodyType by remember { mutableStateOf(userStats.bodyType.ifBlank { "Mezomorf" }) }
    var medicalConditions by remember { mutableStateOf(userStats.medicalConditions) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Editează Profilul",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Prenume") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Nume") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Vârstă") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = height,
                        onValueChange = { height = it },
                        label = { Text("Înălțime (cm)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Greutate (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Gender Selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Gen", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Feminin", "Masculin").forEach { g ->
                            FilterChip(
                                selected = gender == g,
                                onClick = { gender = g },
                                label = { Text(g) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Activity Level Selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Nivel Activitate", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val activities = listOf("Sedentar", "Ușor activ", "Moderat", "Foarte activ")
                    var expandedActivity by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { expandedActivity = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(activityLevel)
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = expandedActivity,
                            onDismissRequest = { expandedActivity = false }
                        ) {
                            activities.forEach { act ->
                                DropdownMenuItem(
                                    text = { Text(act) },
                                    onClick = {
                                        activityLevel = act
                                        expandedActivity = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Goal Selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Obiectiv", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val goals = listOf("Slăbire", "Menținere", "Creștere masă")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        goals.forEach { g ->
                            FilterChip(
                                selected = goal == g,
                                onClick = { goal = g },
                                label = { Text(g) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Body Type Selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Tip Corporal", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    val bodyTypes = listOf("Ectomorf", "Mezomorf", "Endomorf")
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        bodyTypes.forEach { bt ->
                            FilterChip(
                                selected = bodyType == bt,
                                onClick = { bodyType = bt },
                                label = { Text(bt) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = medicalConditions,
                    onValueChange = { medicalConditions = it },
                    label = { Text("Condiții medicale / Alergii (Opțional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ageVal = age.toIntOrNull()
                    val heightVal = height.toFloatOrNull()
                    val weightVal = weight.toFloatOrNull()

                    if (firstName.isBlank() || lastName.isBlank() || ageVal == null || heightVal == null || weightVal == null) {
                        errorMessage = "Vă rugăm să introduceți valori valide pentru câmpurile obligatorii."
                        return@Button
                    }
                    if (ageVal !in 10..120) {
                        errorMessage = "Te rugăm să introduci o vârstă validă (între 10 și 120 de ani)."
                        return@Button
                    }
                    if (heightVal !in 100f..250f) {
                        errorMessage = "Te rugăm să introduci o înălțime validă (între 100 și 250 cm)."
                        return@Button
                    }
                    if (weightVal !in 30f..300f) {
                        errorMessage = "Te rugăm să introduci o greutate validă (între 30 și 300 kg)."
                        return@Button
                    }

                    val updatedStats = userStats.copy(
                        firstName = firstName,
                        lastName = lastName,
                        age = ageVal,
                        height = heightVal,
                        weight = weightVal,
                        gender = gender,
                        activityLevel = activityLevel,
                        goal = goal,
                        bodyType = bodyType,
                        medicalConditions = medicalConditions
                    )
                    
                    val finalStats = com.unitbv.fmi.fitnessapp.logic.FitnessCalculator.calculateMacros(updatedStats)
                    onSave(finalStats)
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
            ) {
                Text("Salvează")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = OliveGreen)
            ) {
                Text("Anulează")
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun InfoCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun TargetCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
