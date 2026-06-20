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
import androidx.compose.material.icons.rounded.MenuBook
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
import com.unitbv.fmi.fitnessapp.models.Recipe
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.unitbv.fmi.fitnessapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
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
                val response1 = HttpHelper.fetchUrl(
                    "https://ro.openfoodfacts.org/cgi/search.pl?search_terms=bio&json=true&page_size=5"
                )
                val response2 = HttpHelper.fetchUrl(
                    "https://ro.openfoodfacts.org/cgi/search.pl?search_terms=fructe&json=true&page_size=5"
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
                                    name.contains("mic dejun", ignoreCase = true) || name.contains("cereale", ignoreCase = true) || name.contains("iaurt", ignoreCase = true) -> "Mic dejun"
                                    name.contains("pranz", ignoreCase = true) || name.contains("supa", ignoreCase = true) || name.contains("orez", ignoreCase = true) || name.contains("paste", ignoreCase = true) -> "Prânz"
                                    name.contains("cina", ignoreCase = true) || name.contains("salata", ignoreCase = true) || name.contains("legume", ignoreCase = true) -> "Cină"
                                    else -> "Snacks"
                                }
                                
                                val ingredients = prod.optString("ingredients_text", "Ingrediente naturale, de calitate.")
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

                parseAndStore(response1)
                parseAndStore(response2)

                if (parsedCount > 0) {
                    loadRecipesFromDb()
                    Toast.makeText(context, "Meniu actualizat cu succes de pe server!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Nu s-au putut încărca date noi din rețea. Se afișează din cache.", Toast.LENGTH_SHORT).show()
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

    LaunchedEffect(Unit) {
        loadRecipesFromDb()
        if (localDb.getAllRecipes().isEmpty()) {
            // Seed DB with high quality Romanian recipes
            val defaultRecipes = listOf(
                Recipe(id = "r1", name = "Omletă cu spanac și brânză", calories = 320, protein = 24, carbs = 5, fats = 22, category = "Mic dejun", ingredients = emptyList(), instructions = "Bate ouăle, adaugă spanacul și brânza. Gătește la foc mic 5 minute.", difficulty = "Ușor", prepTime = 10),
                Recipe(id = "r2", name = "Ovăz cu fructe de pădure", calories = 280, protein = 8, carbs = 45, fats = 6, category = "Mic dejun", ingredients = emptyList(), instructions = "Fierbe ovăzul în lapte. Adaugă fructele de pădure proaspete deasupra.", difficulty = "Ușor", prepTime = 5),
                Recipe(id = "r3", name = "Iaurt grecesc cu miere și nuci", calories = 350, protein = 18, carbs = 30, fats = 18, category = "Mic dejun", ingredients = emptyList(), instructions = "Amestecă iaurtul grecesc cu miere și nuci mărunțite.", difficulty = "Ușor", prepTime = 2),
                
                Recipe(id = "r4", name = "Piept de pui la grătar cu orez", calories = 450, protein = 40, carbs = 50, fats = 8, category = "Prânz", ingredients = emptyList(), instructions = "Rumenește puiul pe grătar. Servește cu orez basmati fiert.", difficulty = "Mediu", prepTime = 25),
                Recipe(id = "r5", name = "Somon la cuptor cu sparanghel", calories = 520, protein = 35, carbs = 10, fats = 35, category = "Prânz", ingredients = emptyList(), instructions = "Asezonează somonul și sparanghelul, bagă la cuptor 20 min la 180C.", difficulty = "Ușor", prepTime = 25),
                Recipe(id = "r6", name = "Salată Caesar cu pui", calories = 380, protein = 30, carbs = 15, fats = 22, category = "Prânz", ingredients = emptyList(), instructions = "Amestecă salata, crutoanele, parmezanul, puiul și dressingul.", difficulty = "Ușor", prepTime = 15),
                
                Recipe(id = "r7", name = "Supă cremă de roșii", calories = 200, protein = 5, carbs = 30, fats = 8, category = "Cină", ingredients = emptyList(), instructions = "Fierbe roșiile cu usturoi și busuioc. Blendează până devine cremă.", difficulty = "Mediu", prepTime = 30),
                Recipe(id = "r8", name = "Friptură de vită cu legume", calories = 600, protein = 45, carbs = 20, fats = 35, category = "Cină", ingredients = emptyList(), instructions = "Gătește vita la foc mare. Trage legumele la tigaie.", difficulty = "Mediu", prepTime = 35),
                Recipe(id = "r9", name = "Salată grecească", calories = 320, protein = 12, carbs = 15, fats = 25, category = "Cină", ingredients = emptyList(), instructions = "Taie roșii, castraveți, ceapă. Adaugă măsline și brânză feta.", difficulty = "Ușor", prepTime = 10),
                
                Recipe(id = "r10", name = "Un măr și migdale (30g)", calories = 250, protein = 7, carbs = 25, fats = 15, category = "Snacks", ingredients = emptyList(), instructions = "Gustare simplă, bogată în vitamine și grăsimi sănătoase.", difficulty = "Ușor", prepTime = 1),
                Recipe(id = "r11", name = "Baton proteic", calories = 200, protein = 20, carbs = 20, fats = 5, category = "Snacks", ingredients = emptyList(), instructions = "Gata de consumat după antrenament.", difficulty = "Ușor", prepTime = 1),
                Recipe(id = "r12", name = "Smoothie verde cu spanac", calories = 150, protein = 4, carbs = 30, fats = 1, category = "Snacks", ingredients = emptyList(), instructions = "Blendează spanac, banană, măr și apă.", difficulty = "Ușor", prepTime = 5)
            )
            defaultRecipes.forEach { localDb.insertRecipe(it) }
            
            // Sync extra products from API
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ingrediente Bio & Rețete",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
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
                        syncRecipesFromNetwork() 
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading || isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(
                            Icons.Default.Refresh, 
                            contentDescription = "Reîmprospătează",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    label = { 
                        Text(
                            text = category,
                            fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedCategory == category) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ForestGreen,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = null
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading && recipes.isEmpty()) {
             Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                 CircularProgressIndicator(color = ForestGreen)
             }
        } else if (recipes.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.CloudOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = LightSage)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Apasă pe butonul de reîmprospătare pentru a încărca rețete online.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    recipe.name, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                AssistChip(
                    onClick = { },
                    label = { Text(recipe.difficulty, color = MaterialTheme.colorScheme.onSecondaryContainer, style = MaterialTheme.typography.labelSmall) },
                    leadingIcon = { 
                        Icon(
                            Icons.Default.Star, 
                            contentDescription = null, 
                            modifier = Modifier.size(14.dp),
                            tint = CalorieAmber
                        ) 
                    },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(8.dp),
                    border = null
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RecipeDetail(Icons.Default.AccessTime, "${recipe.prepTime} min")
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(CalorieAmber)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${recipe.calories} kcal", 
                        style = MaterialTheme.typography.labelMedium, 
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (recipe.instructions.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = recipe.instructions,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val totalMacros = (recipe.protein + recipe.carbs + recipe.fats).toFloat()
            val pWeight = if (totalMacros > 0f) recipe.protein / totalMacros else 0.33f
            val cWeight = if (totalMacros > 0f) recipe.carbs / totalMacros else 0.33f
            val fWeight = if (totalMacros > 0f) recipe.fats / totalMacros else 0.34f
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                if (pWeight > 0) {
                    Box(modifier = Modifier.weight(pWeight).fillMaxHeight().background(ProteinCoral))
                }
                if (cWeight > 0) {
                    Box(modifier = Modifier.weight(cWeight).fillMaxHeight().background(CarbsTeal))
                }
                if (fWeight > 0) {
                    Box(modifier = Modifier.weight(fWeight).fillMaxHeight().background(FatsOrange))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroPill("P: ${recipe.protein}g", ProteinCoralLight, ProteinCoral)
                MacroPill("C: ${recipe.carbs}g", CarbsTealLight, CarbsTeal)
                MacroPill("F: ${recipe.fats}g", FatsOrangeLight, FatsOrange)
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text, 
            style = MaterialTheme.typography.bodySmall, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MacroPill(text: String, bgColor: Color, contentColor: Color) {
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text, 
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
