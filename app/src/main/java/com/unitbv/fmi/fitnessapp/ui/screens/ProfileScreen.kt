package com.unitbv.fmi.fitnessapp.ui.screens

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
import androidx.compose.ui.unit.dp
import com.unitbv.fmi.fitnessapp.data.FirebaseService
import com.unitbv.fmi.fitnessapp.models.UserStats
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    var userStats by remember { mutableStateOf<UserStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        userStats = FirebaseService.getUserProfile()
        isLoading = false
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
                Text("Profilul Meu", style = MaterialTheme.typography.headlineMedium)
                IconButton(onClick = { /* Edit profile */ }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editează")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // User Header
            Card(modifier = Modifier.fillMaxWidth()) {
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
                        Text("${userStats?.firstName} ${userStats?.lastName}", style = MaterialTheme.typography.titleLarge)
                        Text(userStats?.gender ?: "Nespecificat", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Analiză Corporală", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoCard(Modifier.weight(1f), "Greutate", "${userStats?.weight} kg")
                InfoCard(Modifier.weight(1f), "Înălțime", "${userStats?.height} cm")
                InfoCard(Modifier.weight(1f), "BMI", String.format("%.1f", userStats?.bmi ?: 0f))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Obiectiv: ${userStats?.goal}", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Tip corporal: ${userStats?.bodyType}", style = MaterialTheme.typography.bodyMedium)
                    Text("Activitate: ${userStats?.activityLevel}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Setări și Aplicație", style = MaterialTheme.typography.titleMedium)
            
            ListItem(
                headlineContent = { Text("Notificări") },
                trailingContent = { Switch(checked = true, onCheckedChange = {}) }
            )
            ListItem(
                headlineContent = { Text("Unități de măsură") },
                supportingContent = { Text("Metric (kg, cm)") }
            )
            
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
