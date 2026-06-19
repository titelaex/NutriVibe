package com.unitbv.fmi.fitnessapp.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
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
            // Încercăm să salvăm pe server cu un timeout scurt (3 secunde)
            withTimeout(3000) {
                db.collection("users").document(userId).set(stats).await()
            }
            android.util.Log.d("FirebaseService", "Firestore: Profil salvat cu succes pe server")
        } catch (e: Exception) {
            // Dacă expiră sau nu e internet, se salvează local în cache-ul offline
            // și va fi sincronizat automat în fundal când revine conexiunea.
            android.util.Log.w("FirebaseService", "Firestore: Salvare locală asincronă în cache din cauza conexiunii lente/lipsă.")
            db.collection("users").document(userId).set(stats)
        }
    }

    suspend fun getUserProfile(): UserStats? {
        val userId = getCurrentUserId() ?: return null
        
        // 1. Încercăm mai întâi din cache-ul local pentru încărcare instantanee (0-10ms)
        try {
            val cachedDoc = db.collection("users").document(userId).get(Source.CACHE).await()
            if (cachedDoc.exists()) {
                val stats = cachedDoc.toObject(UserStats::class.java)
                if (stats != null) {
                    android.util.Log.d("FirebaseService", "Firestore: Profil încărcat instant din CACHE local")
                    return stats
                }
            }
        } catch (e: Exception) {
            // Profilul nu este în cache, trecem la citirea de pe server
        }

        // 2. Dacă nu este în cache, citim de pe server (timeout 3s)
        return try {
            withTimeout(3000) {
                val doc = db.collection("users").document(userId).get(Source.SERVER).await()
                if (doc.exists()) {
                    doc.toObject(UserStats::class.java)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("FirebaseService", "Firestore: Nu s-a putut lua de pe server (timeout/offline), propagăm excepția.")
            throw e
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun saveMeal(meal: Meal) {
        val userId = getCurrentUserId() ?: throw Exception("Utilizator neautentificat")
        try {
            withTimeout(3000) {
                db.collection("users").document(userId).collection("meals").add(meal).await()
            }
        } catch (e: Exception) {
            android.util.Log.w("FirebaseService", "Firestore: Adăugare masă asincronă în cache local.")
            db.collection("users").document(userId).collection("meals").add(meal)
        }
    }

    suspend fun getTodaysMeals(): List<Meal> {
        val userId = getCurrentUserId() ?: return emptyList()
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = com.google.firebase.Timestamp(calendar.time)
        
        // 1. Încercăm din cache-ul local pentru viteza maximă (0-10ms)
        try {
            val cachedSnapshot = db.collection("users").document(userId).collection("meals")
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .get(Source.CACHE).await()
            if (!cachedSnapshot.isEmpty) {
                android.util.Log.d("FirebaseService", "Firestore: Mese încărcate instant din CACHE local")
                return cachedSnapshot.toObjects(Meal::class.java)
            }
        } catch (e: Exception) {
            // Nu sunt în cache, trecem la server
        }

        // 2. Citim de pe server
        return try {
            withTimeout(3000) {
                val snapshot = db.collection("users").document(userId).collection("meals")
                    .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                    .get(Source.SERVER).await()
                snapshot.toObjects(Meal::class.java)
            }
        } catch (e: Exception) {
            android.util.Log.w("FirebaseService", "Firestore: Nu s-au putut încărca mesele de pe server (timeout/offline).")
            emptyList()
        }
    }
}
