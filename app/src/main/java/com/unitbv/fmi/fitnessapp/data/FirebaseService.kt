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
            // Încercăm să salvăm pe server cu un timeout mai scurt (4 secunde)
            withTimeout(4000) {
                db.collection("users").document(userId).set(stats).await()
            }
            android.util.Log.d("FirebaseService", "Firestore: Profil salvat cu succes pe server")
        } catch (e: Exception) {
            // Dacă expiră sau nu este internet, Firestore salvează local automat în cache-ul offline și va sincroniza mai târziu.
            // Executăm apelul asincron (fără await) pentru a nu bloca utilizatorul și a-i permite să treacă la Dashboard.
            android.util.Log.w("FirebaseService", "Firestore: Salvare pe server blocată sau timeout, se salvează local în offline cache: ${e.message}")
            db.collection("users").document(userId).set(stats)
        }
    }

    suspend fun getUserProfile(): UserStats? {
        val userId = getCurrentUserId() ?: return null
        return try {
            withTimeout(4000) {
                val doc = db.collection("users").document(userId).get().await()
                doc.toObject(UserStats::class.java)
            }
        } catch (e: Exception) {
            android.util.Log.w("FirebaseService", "Firestore: Nu s-a putut citi de pe server, se încearcă din offline cache...")
            try {
                val doc = db.collection("users").document(userId).get(Source.CACHE).await()
                doc.toObject(UserStats::class.java)
            } catch (cacheEx: Exception) {
                android.util.Log.e("FirebaseService", "Firestore: Nici local cache nu e disponibil", cacheEx)
                null
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun saveMeal(meal: Meal) {
        val userId = getCurrentUserId() ?: throw Exception("Utilizator neautentificat")
        try {
            withTimeout(4000) {
                db.collection("users").document(userId).collection("meals").add(meal).await()
            }
        } catch (e: Exception) {
            android.util.Log.w("FirebaseService", "Firestore: Eroare la salvarea mesei online, salvăm în offline cache: ${e.message}")
            db.collection("users").document(userId).collection("meals").add(meal)
        }
    }

    suspend fun getTodaysMeals(): List<Meal> {
        val userId = getCurrentUserId() ?: return emptyList()
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        val startOfDay = com.google.firebase.Timestamp(calendar.time)
        
        return try {
            withTimeout(4000) {
                val snapshot = db.collection("users").document(userId).collection("meals")
                    .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                    .get().await()
                snapshot.toObjects(Meal::class.java)
            }
        } catch (e: Exception) {
            android.util.Log.w("FirebaseService", "Firestore: Eroare la citirea meselor de pe server, se încearcă din cache...")
            try {
                val snapshot = db.collection("users").document(userId).collection("meals")
                    .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                    .get(Source.CACHE).await()
                snapshot.toObjects(Meal::class.java)
            } catch (cacheEx: Exception) {
                emptyList()
            }
        }
    }
}
