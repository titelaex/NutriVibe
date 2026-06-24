package com.unitbv.fmi.fitnessapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.unitbv.fmi.fitnessapp.models.UserStats
import com.unitbv.fmi.fitnessapp.models.Meal
import com.unitbv.fmi.fitnessapp.models.Recipe
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
            db.collection("users").document(userId).set(stats).await()
            android.util.Log.d("FirebaseService", "Firestore: Profil salvat cu succes pe server")
        } catch (e: Exception) {
            android.util.Log.w("FirebaseService", "Firestore: Eroare la salvare pe server, se încearca salvare locala in cache.", e)
            db.collection("users").document(userId).set(stats)
        }
    }

    suspend fun getUserProfile(): UserStats? {
        val userId = getCurrentUserId() ?: return null
        //incercam din cache local
        try {
            val cachedDoc = db.collection("users").document(userId).get(Source.CACHE).await()
            if (cachedDoc.exists()) {
                val stats = cachedDoc.toObject(UserStats::class.java)
                if (stats != null) {
                    android.util.Log.d("FirebaseService", "Firestore: Profil incarcat CACHE local")
                    return stats
                }
            }
        } catch (e: Exception) {
            // Profilul nu este in cache => cit din firestore
        }
        //cit din firestore
        return try {
            val doc = db.collection("users").document(userId).get().await()
            if (doc.exists()) {
                android.util.Log.d("FirebaseService", "Firestore: Profil încărcat de pe server")
                doc.toObject(UserStats::class.java)
            } else {
                android.util.Log.d("FirebaseService", "Firestore: Documentul profilului nu există pe server")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseService", "Firestore: Eroare la citirea profilului de pe server", e)
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun saveMeal(meal: Meal) {
        val userId = getCurrentUserId() ?: throw Exception("Utilizator neautentificat")
        try {
            db.collection("users").document(userId).collection("meals").add(meal).await()
        } catch (e: Exception) {
            android.util.Log.w("FirebaseService", "Firestore: Adaugare masa cache local")
            db.collection("users").document(userId).collection("meals").add(meal)
        }
    }

    suspend fun getMealsForDate(date: java.util.Date): List<Meal> {
        val userId = getCurrentUserId() ?: return emptyList()
        val calendar = java.util.Calendar.getInstance()
        calendar.time = date
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = com.google.firebase.Timestamp(calendar.time)

        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        val endOfDay = com.google.firebase.Timestamp(calendar.time)

        return try {
            val snapshot = db.collection("users").document(userId).collection("meals")
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .whereLessThan("timestamp", endOfDay)
                .get().await()
            snapshot.toObjects(Meal::class.java)
        } catch (e: Exception) {
            android.util.Log.w("FirebaseService", "Firestore: Nu s-au putut încărca mesele pentru data selectată.", e)
            emptyList()
        }
    }

    suspend fun saveCommunityRecipe(recipe: Recipe) {
        try {
            db.collection("community_recipes").document(recipe.id).set(recipe).await()
            android.util.Log.d("FirebaseService", "Firestore: Rețeta a fost salvată pe server")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseService", "Firestore: Eroare la adăugare rețetă", e)
        }
    }

    suspend fun getCommunityRecipes(): List<Recipe> {
        return try {
            val snapshot = db.collection("community_recipes").get().await()
            snapshot.toObjects(Recipe::class.java)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseService", "Firestore: Eroare la citirea rețetelor comunității", e)
            emptyList()
        }
    }
}
