package com.unitbv.fmi.fitnessapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unitbv.fmi.fitnessapp.data.HttpHelper
import com.unitbv.fmi.fitnessapp.data.LocalDbHelper
import com.unitbv.fmi.fitnessapp.models.Recipe
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun RecipesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localDb = remember { LocalDbHelper(context) }
    
    var selectedCategory by remember { mutableStateOf("Toate") }
    val categories = listOf("Toate", "Mic dejun", "Prânz", "Cină", "Snacks")
    
    var recipes by remember { mutableStateOf<List<Recipe>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Load data function
    val loadRecipesFromDb = {
        recipes = localDb.getAllRecipes()
    }

    // Network request and DB saving function
    val syncRecipesFromNetwork = {
        scope.launch {
            isLoading = true
            try {
                // Request 1: Fetch organic products
                val response1 = HttpHelper.fetchUrl(
                    "https://world.openfoodfacts.org/cgi/search.pl?search_terms=organic&json=true&page_size=15"
                )
                
                // Request 2: Fetch green / healthy products
                val response2 = HttpHelper.fetchUrl(
                    "https://world.openfoodfacts.org/cgi/search.pl?search_terms=green&json=true&page_size=15"
                )

                var parsedCount = 0

                val parseAndStore = { responseStr: String? ->
                    if (!responseStr.isNullOrBlank()) {
                        val jsonObject = JSONObject(responseStr)
                        val products = jsonObject.optJSONArray("products")
                        if (products != null) {
                            for (i in 0 until products.length()) {
                                val prod = products.getJSONObject(i)
                                val id = prod.optString("code", prod.optString("_id", java.util.UUID.randomUUID().toString()))
                                val name = prod.optString("product_name", prod.optString("product_name_ro", ""))
                                
                                if (name.isBlank() || name == "null") continue
                                
                                val nutriments = prod.optJSONObject("nutriments")
                                val calories = nutriments?.optDouble("energy-kcal_100g", 0.0)?.toInt() ?: 0
                                val protein = nutriments?.optDouble("proteins_100g", 0.0)?.toInt() ?: 0
                                val carbs = nutriments?.optDouble("carbohydrates_100g", 0.0)?.toInt() ?: 0
                                val fats = nutriments?.optDouble("fat_100g", 0.0)?.toInt() ?: 0
                                
                                val brand = prod.optString("brands", "Bio")
                                val category = when {
                                    name.contains("mic dejun", ignoreCase = true) || name.contains("cereale", ignoreCase = true) || name.contains("biscuiti", ignoreCase = true) -> "Mic dejun"
                                    name.contains("pranz", ignoreCase = true) || name.contains("supa", ignoreCase = true) || name.contains("orez", ignoreCase = true) || name.contains("paste", ignoreCase = true) -> "Prânz"
                                    name.contains("cina", ignoreCase = true) || name.contains("salata", ignoreCase = true) || name.contains("legume", ignoreCase = true) -> "Cină"
                                    else -> "Snacks"
                                }
                                
                                val ingredients = prod.optString("ingredients_text", "Ingrediente naturale, cultivate organic.")
                                val instructions = "Brand: $brand\nIngrediente: $ingredients"
                                
                                val recipe = Recipe(
                                    id = id,
                                    name = name,
                                    calories = calories,
                                    protein = protein,
                                    carbs = carbs,
                                    fats = fats,
                                    category = category,
                                    ingredients = emptyList(),
                                    instructions = instructions,
                                    difficulty = if (calories > 400) "Mediu" else "Ușor",
                                    prepTime = if (calories > 300) 15 else 5
                                )
                                localDb.insertRecipe(recipe)
                                parsedCount++
                            }
                        }
                    }
                }

                // Parse both responses (2 HTTP requests)
                parseAndStore(response1)
                parseAndStore(response2)

                if (parsedCount > 0) {
                    loadRecipesFromDb()
                    Toast.makeText(context, "Meniu actualizat cu succes de pe server!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Nu s-au putut încărca date noi. Se afișează din cache.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("RecipesScreen", "Sync error", e)
                Toast.makeText(context, "Eroare la conexiune. Datele sunt încărcate offline.", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
                isRefreshing = false
            }
        }
    }

    // Load from local database on launch
    LaunchedEffect(Unit) {
        loadRecipesFromDb()
        // If local database is empty, sync from network automatically
        if (localDb.getAllRecipes().isEmpty()) {
            syncRecipesFromNetwork()
        }
    }

    val filteredRecipes = if (selectedCategory == "Toate") {
        recipes
    } else {
        recipes.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ingrediente Bio & Rețete",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            IconButton(
                onClick = { 
                    isRefreshing = true
                    syncRecipesFromNetwork() 
                },
                enabled = !isLoading
            ) {
                if (isLoading || isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        Icons.Default.Refresh, 
                        contentDescription = "Reîmprospătează",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            edgePadding = 0.dp,
            divider = {},
            containerColor = Color.Transparent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[categories.indexOf(selectedCategory)]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            categories.forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = { 
                        Text(
                            text = category,
                            fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedCategory == category) MaterialTheme.colorScheme.primary else Color.Gray
                        ) 
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (recipes.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Apasă pe butonul de reîmprospătare pentru a încărca rețete online.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredRecipes) { recipe ->
                    RecipeCard(recipe)
                }
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    recipe.name, 
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AssistChip(
                    onClick = { },
                    label = { Text(recipe.difficulty, color = MaterialTheme.colorScheme.onSecondaryContainer) },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Star, 
                            contentDescription = null, 
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RecipeDetail(Icons.Default.AccessTime, "${recipe.prepTime} min")
                Text(
                    text = "${recipe.calories} kcal / 100g", 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (recipe.instructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recipe.instructions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val totalMacros = (recipe.protein + recipe.carbs + recipe.fats).toFloat()
            val progress = if (totalMacros > 0f) recipe.protein.toFloat() / totalMacros else 0.33f
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroInfo("Proteine", "${recipe.protein}g")
                MacroInfo("Carbohidrați", "${recipe.carbs}g")
                MacroInfo("Grăsimi", "${recipe.fats}g")
            }
        }
    }
}

@Composable
fun RecipeDetail(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            modifier = Modifier.size(16.dp), 
            tint = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = text, 
            style = MaterialTheme.typography.bodySmall, 
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun MacroInfo(label: String, value: String) {
    Text(
        text = "$label: $value", 
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.secondary
    )
}
