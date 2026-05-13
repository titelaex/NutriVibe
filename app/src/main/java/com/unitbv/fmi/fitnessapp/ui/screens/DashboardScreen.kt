package com.unitbv.fmi.fitnessapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unitbv.fmi.fitnessapp.data.FirebaseService
import com.unitbv.fmi.fitnessapp.models.Meal
import com.unitbv.fmi.fitnessapp.models.UserStats
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen() {
    val scope = rememberCoroutineScope()
    var meals by remember { mutableStateOf<List<Meal>>(emptyList()) }
    var userStats by remember { mutableStateOf<UserStats?>(null) }
    var showAddMealDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                userStats = FirebaseService.getUserProfile()
                meals = FirebaseService.getTodaysMeals()
            } finally {
                isLoading = false
            }
        }
    }

    val consumedCalories = meals.sumOf { it.calories }
    val goalCalories = userStats?.dailyCalories ?: 2000
    val progress = if (goalCalories > 0) consumedCalories.toFloat() / goalCalories else 0f

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddMealDialog = true }) {
                    Text("+", style = MaterialTheme.typography.headlineSmall)
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("Dashboard", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Sumar Calorii", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (progress > 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$consumedCalories / $goalCalories kcal", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Mesele de azi", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                if (meals.isEmpty()) {
                    Text("Nicio masă adăugată azi.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(meals) { meal ->
                            MealItem(meal)
                        }
                    }
                }
            }
        }
    }

    if (showAddMealDialog) {
        AddMealDialog(
            onDismiss = { showAddMealDialog = false },
            onSave = { meal ->
                scope.launch {
                    FirebaseService.saveMeal(meal)
                    meals = FirebaseService.getTodaysMeals()
                    showAddMealDialog = false
                }
            }
        )
    }
}

@Composable
fun MealItem(meal: Meal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(meal.name, style = MaterialTheme.typography.titleSmall)
                Text(meal.type, style = MaterialTheme.typography.bodySmall)
            }
            Text("${meal.calories} kcal", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun AddMealDialog(onDismiss: () -> Unit, onSave: (Meal) -> Unit) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Prânz") }
    val mealTypes = listOf("Mic Dejun", "Prânz", "Cină", "Gustare")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adaugă Masă") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nume Masă") })
                OutlinedTextField(value = calories, onValueChange = { calories = it }, label = { Text("Calorii") })
                
                Text("Tip Masă", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    mealTypes.forEach { mealType ->
                        FilterChip(
                            selected = type == mealType,
                            onClick = { type = mealType },
                            label = { Text(mealType) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val cal = calories.toIntOrNull() ?: 0
                onSave(Meal(name = name, calories = cal, type = type))
            }) {
                Text("Salvează")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anulează") }
        }
    )
}
