package com.unitbv.fmi.fitnessapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.unitbv.fmi.fitnessapp.data.FirebaseService
import com.unitbv.fmi.fitnessapp.models.Meal
import com.unitbv.fmi.fitnessapp.models.UserStats
import kotlinx.coroutines.launch
import com.unitbv.fmi.fitnessapp.ui.theme.*

@Composable
fun DashboardScreen() {
    val scope = rememberCoroutineScope()
    var meals by remember { mutableStateOf<List<Meal>>(emptyList()) }
    var userStats by remember { mutableStateOf<UserStats?>(null) }
    var showAddMealDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val jobStats = scope.launch {
            try {
                userStats = FirebaseService.getUserProfile()
            } catch (e: Exception) {
                android.util.Log.e("DashboardScreen", "Error loading stats", e)
            }
        }
        val jobMeals = scope.launch {
            try {
                meals = FirebaseService.getTodaysMeals()
            } catch (e: Exception) {
                android.util.Log.e("DashboardScreen", "Error loading meals", e)
            }
        }
        scope.launch {
            jobStats.join()
            jobMeals.join()
            isLoading = false
        }
    }

    val consumedCalories = meals.sumOf { it.calories }
    val goalCalories = userStats?.dailyCalories ?: 2000
    val progress = if (goalCalories > 0) consumedCalories.toFloat() / goalCalories else 0f
    
    val totalProtein = meals.sumOf { it.protein }
    val totalCarbs = meals.sumOf { it.carbs }
    val totalFats = meals.sumOf { it.fats }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ForestGreen)
        }
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddMealDialog = true },
                    containerColor = ForestGreen,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Adauga Masa")
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Buna, ${userStats?.firstName ?: "Utilizator"}!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Hai sa urmarim progresul de azi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Calorii Consumate", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            val trackColor = MaterialTheme.colorScheme.surfaceVariant
                            val errorColor = ErrorRed
                            
                            Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.size(180.dp)) {
                                    drawArc(
                                        color = trackColor,
                                        startAngle = 270f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    
                                    if (progress > 1f) {
                                        drawArc(
                                            color = errorColor,
                                            startAngle = 270f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    } else {
                                        drawArc(
                                            brush = Brush.sweepGradient(
                                                colors = listOf(GradientGreenStart, GradientGreenEnd, GradientGreenStart)
                                            ),
                                            startAngle = 270f,
                                            sweepAngle = progress * 360f,
                                            useCenter = false,
                                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$consumedCalories",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = if (progress > 1f) ErrorRed else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "/ $goalCalories kcal",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MacroSummaryCard(Modifier.weight(1f), "Proteine", "${totalProtein}g", Icons.Rounded.FitnessCenter, ProteinCoral)
                                Spacer(modifier = Modifier.width(8.dp))
                                MacroSummaryCard(Modifier.weight(1f), "Carbohidrati", "${totalCarbs}g", Icons.Rounded.Grain, CarbsTeal)
                                Spacer(modifier = Modifier.width(8.dp))
                                MacroSummaryCard(Modifier.weight(1f), "Grasimi", "${totalFats}g", Icons.Rounded.WaterDrop, FatsOrange)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mesele de azi", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (meals.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Rounded.RestaurantMenu, contentDescription = null, modifier = Modifier.size(64.dp), tint = LightSage)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Nicio masă adăugată azi.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(meals) { meal ->
                        MealItem(meal)
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // padding for fab
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
fun MacroSummaryCard(modifier: Modifier = Modifier, label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun MealItem(meal: Meal) {
    val (icon, color) = when (meal.type) {
        "Mic Dejun" -> Icons.Rounded.WbSunny to CalorieAmber
        "Prânz", "Pranz" -> Icons.Rounded.Restaurant to SageGreen
        "Cină", "Cina" -> Icons.Rounded.NightsStay to OliveGreen
        else -> Icons.Rounded.FavoriteBorder to ProteinCoral
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(meal.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(meal.type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${meal.calories} kcal", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealDialog(onDismiss: () -> Unit, onSave: (Meal) -> Unit) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Prânz") }
    val mealTypes = listOf("Mic Dejun", "Prânz", "Cină", "Gustare")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adaugă Masă", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Nume Masă") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = calories, 
                    onValueChange = { calories = it }, 
                    label = { Text("Calorii") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tip Masă", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        mealTypes.take(2).forEach { mealType ->
                            FilterChip(
                                selected = type == mealType,
                                onClick = { type = mealType },
                                label = { Text(mealType) },
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        mealTypes.drop(2).forEach { mealType ->
                            FilterChip(
                                selected = type == mealType,
                                onClick = { type = mealType },
                                label = { Text(mealType) },
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ForestGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = calories.toIntOrNull() ?: 0
                    onSave(Meal(name = name, calories = cal, type = type))
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
            ) {
                Text("Salvează")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = OliveGreen)) { 
                Text("Anulează") 
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}
