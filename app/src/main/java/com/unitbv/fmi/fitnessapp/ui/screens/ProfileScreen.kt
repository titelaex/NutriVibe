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

    var userStats by remember { mutableStateOf<UserStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }

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
                IconButton(onClick = { /* Edit profile placeholder */ }) {
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
                        Text(userStats?.gender ?: "Gen nespecificat", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                InfoCard(Modifier.weight(1f), "Greutate", "${userStats?.weight ?: 0f} kg", Icons.Rounded.MonitorWeight, CalorieAmber)
                InfoCard(Modifier.weight(1f), "Înălțime", "${userStats?.height ?: 0f} cm", Icons.Rounded.Height, CarbsTeal)
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
                    ListItem(
                        headlineContent = { Text("Notificări zilnice") },
                        leadingContent = { Icon(Icons.Rounded.Notifications, contentDescription = null) },
                        trailingContent = { 
                            Switch(
                                checked = notificationsEnabled, 
                                onCheckedChange = { isChecked ->
                                    notificationsEnabled = isChecked
                                    sharedPrefs.edit().putBoolean("notifications_enabled", isChecked).apply()
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = ForestGreen)
                            ) 
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    
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
}

@Composable
fun InfoCard(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
