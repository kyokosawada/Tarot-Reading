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
    private val readingsCollection = firestore.collection("readings")

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
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
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
                    createdAt = userData?.get("createdAt") as? Long
                )
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Save tarot reading to Firestore
    suspend fun saveTarotReading(reading: TarotReading): Result<Unit> {
        return try {
            val userId =
                auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            val readingData = mapOf(
                "id" to reading.id,
                "userId" to userId,
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
                        "dailyMessage" to card.dailyMessage
                    )
                },
                "interpretation" to reading.interpretation,
                "createdAt" to System.currentTimeMillis()
            )
            readingsCollection.document(reading.id).set(readingData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user's tarot readings from Firestore
    fun getUserReadings(limit: Int = 10): Flow<List<TarotReading>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = readingsCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
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
                                    uprightKeywords = "",
                                    reversedKeywords = "",
                                    description = "",
                                    dailyMessage = cardData["dailyMessage"] as? String ?: "",
                                    numerology = null
                                )
                            } ?: emptyList(),
                            interpretation = data["interpretation"] as? String ?: ""
                        )
                    } catch (e: Exception) {
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