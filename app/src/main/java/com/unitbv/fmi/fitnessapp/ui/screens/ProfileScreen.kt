package com.unitbv.fmi.fitnessapp.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.unitbv.fmi.fitnessapp.data.FirebaseService
import com.unitbv.fmi.fitnessapp.models.UserStats
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("fitness_prefs", Context.MODE_PRIVATE) }

    var userStats by remember { mutableStateOf<UserStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Read preferences stored in SharedPreferences
    var notificationsEnabled by remember {
        mutableStateOf(sharedPrefs.getBoolean("notifications_enabled", true))
    }
    var metricUnits by remember {
        mutableStateOf(sharedPrefs.getString("unit_system", "Metric (kg, cm)") ?: "Metric (kg, cm)")
    }

    LaunchedEffect(Unit) {
        try {
            userStats = FirebaseService.getUserProfile()
        } catch (e: Exception) {
            android.util.Log.e("ProfileScreen", "Failed to load profile", e)
        } finally {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Profilul Meu", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = { /* Edit profile placeholder */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editează", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // User Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        val fullName = if (userStats != null && !userStats?.firstName.isNullOrBlank()) {
                            "${userStats?.firstName} ${userStats?.lastName}"
                        } else {
                            "Utilizator Nou"
                        }
                        Text(fullName, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        Text(userStats?.gender ?: "Gen nespecificat", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Analiză Corporală", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoCard(Modifier.weight(1f), "Greutate", "${userStats?.weight ?: 0f} kg")
                InfoCard(Modifier.weight(1f), "Înălțime", "${userStats?.height ?: 0f} cm")
                InfoCard(Modifier.weight(1f), "BMI", String.format("%.1f", userStats?.bmi ?: 0f))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Obiectiv: ${userStats?.goal ?: "Menținere"}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tip corporal: ${userStats?.bodyType ?: "Mezomorf"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Activitate: ${userStats?.activityLevel ?: "Moderat"}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Setări și Aplicație (Persistate Local)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            
            ListItem(
                headlineContent = { Text("Notificări zilnice") },
                trailingContent = { 
                    Switch(
                        checked = notificationsEnabled, 
                        onCheckedChange = { isChecked ->
                            notificationsEnabled = isChecked
                            sharedPrefs.edit().putBoolean("notifications_enabled", isChecked).apply()
                        }
                    ) 
                }
            )
            
            var expandedUnits by remember { mutableStateOf(false) }
            Box {
                ListItem(
                    headlineContent = { Text("Unități de măsură") },
                    supportingContent = { Text(metricUnits) },
                    modifier = Modifier.clickable { expandedUnits = true }
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { onLogout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Deconectare")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InfoCard(modifier: Modifier, label: String, value: String) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.titleMedium)
        }
    }
}
