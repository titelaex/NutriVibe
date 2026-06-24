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
import androidx.compose.material.icons.rounded.Add
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
import com.unitbv.fmi.fitnessapp.data.FirebaseService
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
                // Am scos clearRecipes() pentru a nu șterge rețetele hardcodate (fallback-ul românesc)
                // Datele noi din Firestore se vor adăuga peste cele existente.
                
                val serverRecipes = FirebaseService.getCommunityRecipes()

                if (serverRecipes.isNotEmpty()) {
                    for (recipe in serverRecipes) {
                        localDb.insertRecipe(recipe)
                    }
                    loadRecipesFromDb()
                    Toast.makeText(context, "Meniu actualizat cu succes de pe server!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Nu s-au găsit rețete noi în rețea. Se afișează cele implicite.", Toast.LENGTH_SHORT).show()
                    loadRecipesFromDb()
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

    var showAddRecipeDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddRecipeDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Adaugă rețetă")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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

    if (showAddRecipeDialog) {
        AddRecipeDialog(
            onDismiss = { showAddRecipeDialog = false },
            onSave = { newRecipe ->
                scope.launch {
                    try {
                        FirebaseService.saveCommunityRecipe(newRecipe)
                        Toast.makeText(context, "Rețetă publicată cu succes pe rețea!", Toast.LENGTH_SHORT).show()
                        showAddRecipeDialog = false
                        syncRecipesFromNetwork() // Refresh the list
                    } catch (e: Exception) {
                        Toast.makeText(context, "Eroare la salvarea rețetei", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}
}

@Composable
fun AddRecipeDialog(
    onDismiss: () -> Unit,
    onSave: (Recipe) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Prânz") }
    var calories by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publică o Rețetă Nouă") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nume rețetă") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categorie (ex: Mic dejun, Prânz, Cină)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calorii") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Mod de preparare scurt") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cal = calories.toIntOrNull() ?: 0
                    if (name.isNotBlank()) {
                        val newRecipe = Recipe(
                            id = java.util.UUID.randomUUID().toString(),
                            name = name,
                            calories = cal,
                            category = category,
                            instructions = instructions,
                            difficulty = "Ușor",
                            prepTime = 10,
                            protein = 10, carbs = 10, fats = 10
                        )
                        onSave(newRecipe)
                    }
                }
            ) {
                Text("Publică")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anulează")
            }
        }
    )
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
            
            if (recipe.id.length > 5) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Publicat de alți utilizatori",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
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
