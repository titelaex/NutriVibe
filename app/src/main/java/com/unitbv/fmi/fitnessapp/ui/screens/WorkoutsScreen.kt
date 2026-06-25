package com.unitbv.fmi.fitnessapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unitbv.fmi.fitnessapp.data.HttpHelper
import com.unitbv.fmi.fitnessapp.data.LocalDbHelper
import com.unitbv.fmi.fitnessapp.models.Workout
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONArray
import com.unitbv.fmi.fitnessapp.ui.theme.*
import android.text.Html

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localDb = remember { LocalDbHelper(context) }
    
    var workouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val calendar = remember { java.util.Calendar.getInstance() }
    val todayFormatted = remember {
        java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.forLanguageTag("ro-RO")).format(calendar.time)
    }
    val subtitle = "Program personalizat pentru $todayFormatted"

    val loadWorkoutsFromDb = {
        workouts = localDb.getAllWorkouts()
    }

    var quoteOfTheDay by remember { mutableStateOf("") }
    var quoteAuthor by remember { mutableStateOf("") }

    val syncWorkoutsFromNetwork = {
        scope.launch {
            isLoading = true
            try {
                val response1 = HttpHelper.fetchUrl(

                    "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/dist/exercises.json"
                )

                val quoteResponse = HttpHelper.fetchUrl(
                    "https://dummyjson.com/quotes/random"
                )
                
                if (!quoteResponse.isNullOrBlank()) {
                    try {
                        val qJson = JSONObject(quoteResponse)
                        quoteOfTheDay = qJson.optString("quote", "Nu te opri când ești obosit, oprește-te când ai terminat.")
                        quoteAuthor = "- " + qJson.optString("author", "NutriVibe")
                    } catch(e: Exception) {
                        e.printStackTrace()
                    }
                }

                var parsedCount = 0

                if (!response1.isNullOrBlank()) {
                    localDb.clearWorkouts()
                    
                    val jsonArray = org.json.JSONArray(response1)
                    val len = jsonArray.length()
                    if (len > 0) {
                        val randomIndices = mutableSetOf<Int>()
                        val rand = java.util.Random()
                        val targetSize = minOf(4, len)
                        
                        while (randomIndices.size < targetSize) {
                            randomIndices.add(rand.nextInt(len))
                        }
                        
                        for (index in randomIndices) {
                            val item = jsonArray.getJSONObject(index)
                            val id = item.optString("id", java.util.UUID.randomUUID().toString())
                            val name = item.optString("name", "Exercise")
                            val category = item.optString("category", "strength")
                            val level = item.optString("level", "beginner")
                            
                            val instructionsArray = item.optJSONArray("instructions")
                            val instructionsList = mutableListOf<String>()
                            if (instructionsArray != null) {
                                for (i in 0 until instructionsArray.length()) {
                                    instructionsList.add(instructionsArray.optString(i))
                                }
                            }
                            
                            val sets = (3..5).random()
                            val reps = listOf(8, 10, 12, 15).random()
                            val repsText = if (category.lowercase().contains("stretch") || category.lowercase().contains("yoga") || category.lowercase().contains("cardio")) {
                                listOf("30s", "45s", "60s").random()
                            } else {
                                "$reps reps"
                            }
                            val setsRepsPrefix = "$sets sets x $repsText"
                            
                            val rawInstruction = if (instructionsList.isNotEmpty()) {
                                instructionsList.first()
                            } else {
                                "No description available."
                            }
                            val cleanInstruction = if (rawInstruction.length > 100) {
                                rawInstruction.substring(0, 97) + "..."
                            } else {
                                rawInstruction
                            }
                            
                            val description = "$setsRepsPrefix | $cleanInstruction"
                            
                            val difficulty = when (level.lowercase()) {
                                "beginner" -> "Beginner"
                                "intermediate" -> "Intermediate"
                                "expert" -> "Expert"
                                else -> "Intermediate"
                            }
                            
                            val durationMin = when (difficulty) {
                                "Beginner" -> 10
                                "Intermediate" -> 15
                                else -> 20
                            }
                            
                            val workout = Workout(
                                id = id,
                                name = name,
                                description = description,
                                category = category.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.forLanguageTag("ro-RO")) else it.toString() },
                                durationMin = durationMin,
                                difficulty = difficulty
                            )
                            localDb.insertWorkout(workout)
                            parsedCount++
                        }
                    }
                }

                if (parsedCount > 0) {
                    val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    val sharedPrefs = context.getSharedPreferences("fitness_prefs", android.content.Context.MODE_PRIVATE)
                    sharedPrefs.edit().putString("last_workout_sync_date", todayStr).apply()
                    loadWorkoutsFromDb()
                    Toast.makeText(context, "Antrenament generat de pe rețea!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Nu s-au putut prelua exercițiile. Se afișează din cache.", Toast.LENGTH_SHORT).show()
                    loadWorkoutsFromDb()
                }
            } catch (e: Exception) {
                android.util.Log.e("WorkoutsScreen", "Sync error", e)
                Toast.makeText(context, "Eroare rețea. Mod offline.", Toast.LENGTH_SHORT).show()
                loadWorkoutsFromDb()
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        val sharedPrefs = context.getSharedPreferences("fitness_prefs", android.content.Context.MODE_PRIVATE)
        val lastSyncDate = sharedPrefs.getString("last_workout_sync_date", "")
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        
        loadWorkoutsFromDb()
        if (workouts.isEmpty() || lastSyncDate != todayStr) {
            syncWorkoutsFromNetwork()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Antrenamentul Zilei",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    IconButton(
                        onClick = { 
                            isRefreshing = true
                            syncWorkoutsFromNetwork() 
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading || isRefreshing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                        } else {
                            Icon(
                                Icons.Default.Refresh, 
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            if (quoteOfTheDay.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💡 Citatul Zilei",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"$quoteOfTheDay\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = quoteAuthor,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            if (isLoading && workouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (workouts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.CloudOff,
                            contentDescription = "Offline",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Nu ai niciun exercițiu salvat.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(workouts) { workout ->
                        WorkoutCard(workout = workout)
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutCard(workout: Workout) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FitnessCenter,
                    contentDescription = "Workout",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))

                val parts = workout.description.split(" | ")
                val setsReps = parts.getOrNull(0) ?: "3 sets x 12 reps"
                
                Text(
                    text = setsReps,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = workout.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
