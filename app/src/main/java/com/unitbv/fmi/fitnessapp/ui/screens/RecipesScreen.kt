package com.unitbv.fmi.fitnessapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.unitbv.fmi.fitnessapp.models.Recipe

@Composable
fun RecipesScreen() {
    var selectedCategory by remember { mutableStateOf("Toate") }
    val categories = listOf("Toate", "Mic dejun", "Prânz", "Cină", "Snacks")

    // Mock data for recipes
    val allRecipes = listOf(
        Recipe("1", "Omletă cu Spanac", 350, 20, 10, 5, "Mic dejun", emptyList(), "", "Ușor", 15),
        Recipe("2", "Pui la Grătar cu Orez", 550, 45, 40, 10, "Prânz", emptyList(), "", "Mediu", 30),
        Recipe("3", "Salată Grecească", 300, 10, 15, 20, "Cină", emptyList(), "", "Ușor", 10),
        Recipe("4", "Smoothie Proteic", 250, 25, 30, 5, "Mic dejun", emptyList(), "", "Ușor", 5),
        Recipe("5", "Paste Integrale Bolognese", 600, 35, 70, 15, "Prânz", emptyList(), "", "Mediu", 45)
    )

    val filteredRecipes = if (selectedCategory == "Toate") allRecipes else allRecipes.filter { it.category == selectedCategory }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Rețete Nutritive", style = MaterialTheme.typography.headlineMedium)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            edgePadding = 0.dp,
            divider = {},
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) {
            categories.forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = { Text(category) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(filteredRecipes) { recipe ->
                RecipeCard(recipe)
            }
        }
    }
}

@Composable
fun RecipeCard(recipe: Recipe) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(recipe.name, style = MaterialTheme.typography.titleLarge)
                AssistChip(
                    onClick = { },
                    label = { Text(recipe.difficulty) },
                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RecipeDetail(Icons.Default.AccessTime, "${recipe.prepTime} min")
                Text("${recipe.calories} kcal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { 0.5f }, // Placeholder for macro distribution
                modifier = Modifier.fillMaxWidth().height(4.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroInfo("P", "${recipe.protein}g")
                MacroInfo("C", "${recipe.carbs}g")
                MacroInfo("F", "${recipe.fats}g")
            }
        }
    }
}

@Composable
fun RecipeDetail(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = androidx.compose.ui.graphics.Color.Gray)
        Text(text, style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)
    }
}

@Composable
fun MacroInfo(label: String, value: String) {
    Text("$label: $value", style = MaterialTheme.typography.bodySmall)
}
