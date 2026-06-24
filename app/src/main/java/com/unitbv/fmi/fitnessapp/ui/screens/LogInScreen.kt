package com.unitbv.fmi.fitnessapp.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.unitbv.fmi.fitnessapp.ui.theme.*

@Composable
fun LogInScreen(modifier: Modifier = Modifier, onLoginSuccess: (Boolean) -> Unit = {}, onRegister: (String, String) -> Unit = { _, _ -> }) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    var isRegisterMode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        auth.signOut() // Deconectare forțată la deschiderea aplicației
    }

    if (isLoading && auth.currentUser != null && email.isEmpty() && password.isEmpty()) {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = ForestGreen)
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Header Section
            Icon(
                imageVector = Icons.Rounded.Eco,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = SageGreen
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "NutriVibe",
                color = ForestGreen,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Alimentatia ta inteligenta",
                color = OliveGreen,
                style = MaterialTheme.typography.bodyLarge
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            // Form Section
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRegisterMode) "Creare Cont" else "Autentificare",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (errorMessage != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }



                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Parolă") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        enabled = !isLoading,
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = if (passwordVisible) "Ascunde parola" else "Arata parola")
                            }
                        }
                    )
                    
                    if (!isRegisterMode) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            TextButton(
                                onClick = {
                                    if (email.isBlank()) {
                                        errorMessage = "Introdu adresa de email pentru a-ți putea reseta parola."
                                    } else {
                                        isLoading = true
                                        auth.sendPasswordResetEmail(email)
                                            .addOnCompleteListener { task ->
                                                isLoading = false
                                                if (task.isSuccessful) {
                                                    Toast.makeText(context, "Email-ul de resetare a fost trimis! Verifică inbox-ul.", Toast.LENGTH_LONG).show()
                                                } else {
                                                    errorMessage = "Eroare: ${task.exception?.localizedMessage}"
                                                }
                                            }
                                    }
                                },
                                enabled = !isLoading
                            ) {
                                Text("Ai uitat parola?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    } else {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    if (isRegisterMode) {
                                        // Nu creăm contul aici — doar validăm și trimitem la onboarding
                                        if (password.length < 6) {
                                            errorMessage = "Parola trebuie să aibă cel puțin 6 caractere."
                                        } else {
                                            onRegister(email, password)
                                        }} else {
                                        val result = auth.signInWithEmailAndPassword(email, password).await()
                                        val userId = result.user?.uid
                                        val sharedPrefs = context.getSharedPreferences("fitness_prefs", android.content.Context.MODE_PRIVATE)
                                        val hasOnboardedLocal = sharedPrefs.getBoolean("has_completed_onboarding_$userId", false)
                                        
                                        if (hasOnboardedLocal) {
                                            // Deja a completat onboarding-ul înainte, trimite la Dashboard
                                            onLoginSuccess(false)
                                        } else {
                                            // Verificăm pe server dacă are profil
                                            var hasProfile = false
                                            try {
                                                val profile = com.unitbv.fmi.fitnessapp.data.FirebaseService.getUserProfile()
                                                if (profile != null) {
                                                    sharedPrefs.edit().putBoolean("has_completed_onboarding_$userId", true).apply()
                                                    hasProfile = true
                                                }
                                            } catch (e: Exception) {
                                                // Eroare de rețea → presupunem că are profil (mai bine Dashboard gol decât onboarding repetat)
                                                hasProfile = true
                                            }
                                            onLoginSuccess(!hasProfile)
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.localizedMessage ?: "A apărut o eroare"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(if (isRegisterMode) "Înregistrare" else "Conectare", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { isRegisterMode = !isRegisterMode }, 
                enabled = !isLoading,
                colors = ButtonDefaults.textButtonColors(contentColor = OliveGreen)
            ) {
                Text(if (isRegisterMode) "Ai deja un cont? Conectează-te" else "Nu ai cont? Înregistrează-te")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LogInScreenPreview() {
    UnitBvFMI2026Theme {
        LogInScreen()
    }
}