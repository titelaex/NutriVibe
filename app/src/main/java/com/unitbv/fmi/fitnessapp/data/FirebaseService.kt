package com.unitbv.fmi.fitnessapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.unitbv.fmi.fitnessapp.models.UserStats
import com.unitbv.fmi.fitnessapp.models.Meal
import kotlinx.coroutines.tasks.await

import kotlinx.coroutines.withTimeout

object FirebaseService {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun saveUserProfile(stats: UserStats) {
        val userId = getCurrentUserId() ?: throw Exception("Utilizator neautentificat")
        android.util.Log.d("FirebaseService", "Attempting to save profile for: $userId")
        
        try {
            // Timeout de 10 secunde pentru a nu râmâne blocat la infinit
            withTimeout(10000) {
                db.collection("users").document(userId).set(stats).await()
            }
            android.util.Log.d("FirebaseService", "Firestore: Salvare reușită")
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            android.util.Log.e("FirebaseService", "Firestore: Timeout depășit (10s)")
            throw Exception("Conexiunea la server a expirat. Verifică internetul.")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseService", "Firestore: Eroare la salvare - ${e.message}")
            throw e
        }
    }

    suspend fun getUserProfile(): UserStats? {
        val userId = getCurrentUserId() ?: return null
        val doc = db.collection("users").document(userId).get().await()
        return doc.toObject(UserStats::class.java)
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun saveMeal(meal: Meal) {
        val userId = getCurrentUserId() ?: throw Exception("Utilizator neautentificat")
        db.collection("users").document(userId).collection("meals").add(meal).await()
    }

    suspend fun getTodaysMeals(): List<Meal> {
        val userId = getCurrentUserId() ?: return emptyList()
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        val startOfDay = com.google.firebase.Timestamp(calendar.time)
        
        val snapshot = db.collection("users").document(userId).collection("meals")
            .whereGreaterThanOrEqualTo("timestamp", startOfDay)
            .get().await()
        
        return snapshot.toObjects(Meal::class.java)
    }
    
    // Additional methods for meals, recipes, etc. will go here
}
