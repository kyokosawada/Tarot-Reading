package com.example.tarot.data

import com.example.tarot.data.model.TarotCard
import com.example.tarot.viewmodel.TarotReading
import com.example.tarot.viewmodel.User
import com.example.tarot.viewmodel.UserStats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Collections
    private val usersCollection = firestore.collection("users")
    // No global readings collection; readings are stored in per-user subcollections

    // Save user profile to Firestore
    suspend fun saveUserProfile(user: User): Result<Unit> {
        return try {
            val userData = mapOf(
                "id" to user.id,
                "name" to user.name,
                "email" to user.email,
                "username" to user.username,
                "birthMonth" to user.birthMonth,
                "birthYear" to user.birthYear,
                "isProfileComplete" to user.isProfileComplete,
                "createdAt" to (user.createdAt
                    ?: System.currentTimeMillis()), // Preserve original createdAt
                "updatedAt" to System.currentTimeMillis(), // Always update this
                // Journey data fields
                "totalReadings" to user.totalReadings,
                "currentStreak" to user.currentStreak,
                "level" to user.level
            )
            usersCollection.document(user.id).set(userData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user profile from Firestore
    suspend fun getUserProfile(userId: String): Result<User?> {
        return try {
            val document = usersCollection.document(userId).get().await()
            if (document.exists()) {
                val userData = document.data
                val user = User(
                    id = userData?.get("id") as? String ?: userId,
                    name = userData?.get("name") as? String ?: "",
                    email = userData?.get("email") as? String ?: "",
                    username = userData?.get("username") as? String,
                    birthMonth = (userData?.get("birthMonth") as? Long)?.toInt(),
                    birthYear = (userData?.get("birthYear") as? Long)?.toInt(),
                    isProfileComplete = userData?.get("isProfileComplete") as? Boolean ?: false,
                    createdAt = userData?.get("createdAt") as? Long,
                    // Journey data fields
                    totalReadings = (userData?.get("totalReadings") as? Long)?.toInt() ?: 0,
                    currentStreak = (userData?.get("currentStreak") as? Long)?.toInt() ?: 0,
                    level = userData?.get("level") as? String ?: "Novice"
                )
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Save tarot reading to user's subcollection
    suspend fun saveTarotReading(reading: TarotReading): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
            android.util.Log.d("FirebaseRepository", "🔍 saveTarotReading called")
            android.util.Log.d("FirebaseRepository", "📝 Reading ID: ${reading.id}")
            android.util.Log.d("FirebaseRepository", "👤 Current user ID: $userId")

            if (userId == null) {
                android.util.Log.e("FirebaseRepository", "❌ User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            android.util.Log.d("FirebaseRepository", "📊 Preparing reading data...")
            val readingData = mapOf(
                "id" to reading.id,
                "type" to reading.type,
                "title" to reading.title,
                "date" to reading.date,
                "cards" to reading.cards.map { card ->
                    mapOf(
                        "id" to card.id,
                        "name" to card.name,
                        "suit" to card.suit?.name,
                        "cardType" to card.cardType.name,
                        "uprightMeaning" to card.uprightMeaning,
                        "reversedMeaning" to card.reversedMeaning,
                        "imageName" to card.imageName,
                        "uprightKeywords" to card.uprightKeywords,
                        "reversedKeywords" to card.reversedKeywords,
                        "description" to card.description,
                        "uprightDailyMessage" to card.uprightDailyMessage,
                        "reversedDailyMessage" to card.reversedDailyMessage
                    )
                },
                "interpretation" to reading.interpretation,
                "journalNotes" to reading.journalNotes,
                "createdAt" to System.currentTimeMillis()
            )

            android.util.Log.d("FirebaseRepository", "💾 Saving to user's readings subcollection...")
            // Save to user's readings subcollection instead of global collection
            usersCollection.document(userId)
                .collection("readings")
                .document(reading.id)
                .set(readingData)
                .await()

            android.util.Log.d("FirebaseRepository", "✅ Successfully saved reading: ${reading.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseRepository", "❌ Error saving reading: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Update journal notes for a specific reading
    suspend fun updateJournalNotes(readingId: String, notes: String): Result<Unit> {
        return try {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                android.util.Log.e("FirebaseRepository", "❌ User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            android.util.Log.d(
                "FirebaseRepository",
                "📝 Updating journal notes for reading: $readingId"
            )

            usersCollection.document(userId)
                .collection("readings")
                .document(readingId)
                .update("journalNotes", notes)
                .await()

            android.util.Log.d("FirebaseRepository", "✅ Successfully updated journal notes")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e(
                "FirebaseRepository",
                "❌ Error updating journal notes: ${e.message}",
                e
            )
            Result.failure(e)
        }
    }

    // Get user's tarot readings from Firestore (from per-user subcollection)
    fun getUserReadings(limit: Int = 10): Flow<List<TarotReading>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = usersCollection
            .document(userId)
            .collection("readings")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle specific Firestore errors gracefully
                    when {
                        error.message?.contains("requires an index") == true -> {
                            // Index missing - return empty list instead of crashing
                            android.util.Log.w(
                                "FirebaseRepository",
                                "Firestore index missing for readings query. Returning empty list."
                            )
                            trySend(emptyList())
                        }

                        error.message?.contains("PERMISSION_DENIED") == true -> {
                            // Permission denied - return empty list
                            android.util.Log.w(
                                "FirebaseRepository",
                                "Permission denied for readings query. Returning empty list."
                            )
                            trySend(emptyList())
                        }

                        else -> {
                            // Other errors - log and return empty list
                            android.util.Log.e(
                                "FirebaseRepository",
                                "Error loading readings: ${error.message}",
                                error
                            )
                            trySend(emptyList())
                        }
                    }
                    return@addSnapshotListener
                }

                val readings = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data ?: return@mapNotNull null
                        TarotReading(
                            id = data["id"] as? String ?: doc.id,
                            type = data["type"] as? String ?: "",
                            title = data["title"] as? String ?: "",
                            date = data["date"] as? String ?: "",
                            cards = (data["cards"] as? List<Map<String, Any>>)?.map { cardData ->
                                TarotCard(
                                    id = (cardData["id"] as? Long)?.toInt() ?: 0,
                                    name = cardData["name"] as? String ?: "",
                                    suit = cardData["suit"]?.let {
                                        try {
                                            com.example.tarot.data.model.Suit.valueOf(it as String)
                                        } catch (e: Exception) {
                                            null
                                        }
                                    },
                                    cardType = cardData["cardType"]?.let {
                                        try {
                                            com.example.tarot.data.model.CardType.valueOf(it as String)
                                        } catch (e: Exception) {
                                            com.example.tarot.data.model.CardType.MAJOR_ARCANA
                                        }
                                    } ?: com.example.tarot.data.model.CardType.MAJOR_ARCANA,
                                    imageName = cardData["imageName"] as? String ?: "",
                                    uprightMeaning = cardData["uprightMeaning"] as? String ?: "",
                                    reversedMeaning = cardData["reversedMeaning"] as? String ?: "",
                                    uprightKeywords = cardData["uprightKeywords"] as? String ?: "",
                                    reversedKeywords = cardData["reversedKeywords"] as? String
                                        ?: "",
                                    description = cardData["description"] as? String ?: "",
                                    uprightDailyMessage = cardData["uprightDailyMessage"] as? String
                                        ?: "",
                                    reversedDailyMessage = cardData["reversedDailyMessage"] as? String
                                        ?: "",
                                    numerology = null
                                )
                            } ?: emptyList(),
                            interpretation = data["interpretation"] as? String ?: "",
                            journalNotes = data["journalNotes"] as? String ?: ""
                        )
                    } catch (e: Exception) {
                        android.util.Log.e(
                            "FirebaseRepository",
                            "Error parsing reading document: ${e.message}",
                            e
                        )
                        null
                    }
                } ?: emptyList()

                trySend(readings)
            }

        awaitClose { listener.remove() }
    }

    // Save user stats to Firestore
    suspend fun saveUserStats(userId: String, stats: UserStats): Result<Unit> {
        return try {
            val statsData = mapOf(
                "totalReadings" to stats.totalReadings,
                "currentStreak" to stats.currentStreak,
                "level" to stats.level,
                "experiencePoints" to stats.experiencePoints,
                "updatedAt" to System.currentTimeMillis()
            )
            usersCollection.document(userId).collection("stats").document("current")
                .set(statsData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user stats from Firestore
    suspend fun getUserStats(userId: String): Result<UserStats?> {
        return try {
            val document = usersCollection.document(userId).collection("stats")
                .document("current").get().await()
            if (document.exists()) {
                val data = document.data
                val stats = UserStats(
                    totalReadings = (data?.get("totalReadings") as? Long)?.toInt() ?: 0,
                    currentStreak = (data?.get("currentStreak") as? Long)?.toInt() ?: 0,
                    level = data?.get("level") as? String ?: "Novice",
                    experiencePoints = (data?.get("experiencePoints") as? Long)?.toInt() ?: 0
                )
                Result.success(stats)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}