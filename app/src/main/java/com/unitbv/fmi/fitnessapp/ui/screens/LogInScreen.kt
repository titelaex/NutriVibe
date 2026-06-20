package com.unitbv.fmi.fitnessapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
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
fun LogInScreen(modifier: Modifier = Modifier, onLoginSuccess: (Boolean) -> Unit = {}) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    
    var isRegisterMode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val sharedPrefs = context.getSharedPreferences("fitness_prefs", android.content.Context.MODE_PRIVATE)
            val hasOnboardedLocal = sharedPrefs.getBoolean("has_completed_onboarding_$userId", false)

            if (hasOnboardedLocal) {
                onLoginSuccess(false) // Go to Dashboard
            } else {
                isLoading = true
                try {
                    val profile = com.unitbv.fmi.fitnessapp.data.FirebaseService.getUserProfile()
                    if (profile != null) {
                        sharedPrefs.edit().putBoolean("has_completed_onboarding_$userId", true).apply()
                        onLoginSuccess(false) // Go to Dashboard
                    } else {
                        onLoginSuccess(true)  // Go to Onboarding
                    }
                } catch (e: Exception) {
                    // Network error / timeout: assume they have a profile (offline mode)
                    onLoginSuccess(false)
                } finally {
                    isLoading = false
                }
            }
        }
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

                    AnimatedVisibility(
                        visible = isRegisterMode,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            OutlinedTextField(
                                value = firstName,
                                onValueChange = { firstName = it },
                                label = { Text("Prenume") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                enabled = !isLoading
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                value = lastName,
                                onValueChange = { lastName = it },
                                label = { Text("Nume") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                enabled = !isLoading
                            )
                            Spacer(modifier = Modifier.height(16.dp))
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
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                try {
                                    if (isRegisterMode) {
                                        val result = auth.createUserWithEmailAndPassword(email, password).await()
                                        val profileUpdates = UserProfileChangeRequest.Builder()
                                            .setDisplayName("$firstName $lastName")
                                            .build()
                                        result.user?.updateProfile(profileUpdates)?.await()
                                        onLoginSuccess(true)
                                    } else {
                                        val result = auth.signInWithEmailAndPassword(email, password).await()
                                        val userId = result.user?.uid
                                        val sharedPrefs = context.getSharedPreferences("fitness_prefs", android.content.Context.MODE_PRIVATE)
                                        var hasProfile = false
                                        try {
                                            val profile = com.unitbv.fmi.fitnessapp.data.FirebaseService.getUserProfile()
                                            if (profile != null) {
                                                sharedPrefs.edit().putBoolean("has_completed_onboarding_$userId", true).apply()
                                                hasProfile = true
                                            }
                                        } catch (e: Exception) {
                                            hasProfile = true // If network error, default to Dashboard (offline cache)
                                        }
                                        onLoginSuccess(!hasProfile)
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
                        enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty() && (!isRegisterMode || (firstName.isNotEmpty() && lastName.isNotEmpty()))
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