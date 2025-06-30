package com.example.tarot.data.repository

import android.util.Log
import com.example.tarot.data.FirebaseRepository
import com.example.tarot.viewmodel.AuthViewModel
import com.example.tarot.viewmodel.User
import com.example.tarot.viewmodel.UserStats
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JourneyRepository @Inject constructor(
    private val firebaseRepository: FirebaseRepository,
    private val tarotRepository: TarotRepository
) {
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "JourneyRepository"
    }

    // Increment reading count and update streak based on daily reading
    suspend fun incrementReading(): Result<User?> {
        return try {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId != null) {
                Log.d(TAG, "Incrementing reading for user: $currentUserId")

                firebaseRepository.getUserProfile(currentUserId).fold(
                    onSuccess = { user ->
                        if (user != null) {
                            val todayDate = getTodayDateString()
                            Log.d(
                                TAG,
                                "Today's date: $todayDate, Last reading date: ${user.lastReadingDate}"
                            )

                            // Calculate new streak
                            val streakData = calculateNewStreak(user, todayDate)
                            val newReadingCount = user.totalReadings + 1
                            val newLevel = AuthViewModel.calculateUserLevel(newReadingCount)

                            Log.d(
                                TAG,
                                "Streak calculation: current=${streakData.currentStreak}"
                            )

                            // Update user with new journey data
                            val updatedUser = user.copy(
                                totalReadings = newReadingCount,
                                currentStreak = streakData.currentStreak,
                                lastReadingDate = todayDate,
                                level = newLevel
                            )

                            // Save updated user profile
                            firebaseRepository.saveUserProfile(updatedUser).fold(
                                onSuccess = {
                                    Log.d(TAG, "Successfully updated user journey")
                                    Result.success(updatedUser)
                                },
                                onFailure = { error ->
                                    Log.e(TAG, "Failed to save user profile", error)
                                    Result.failure(error)
                                }
                            )
                        } else {
                            Log.e(TAG, "User profile not found")
                            Result.failure(Exception("User profile not found"))
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to get user profile", error)
                        Result.failure(error)
                    }
                )
            } else {
                Log.e(TAG, "User not authenticated")
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementing reading", e)
            Result.failure(e)
        }
    }

    // Calculate streak based on last reading date
    private fun calculateNewStreak(user: User, todayDate: String): StreakData {
        val lastReadingDate = user.lastReadingDate
        val currentStreak = user.currentStreak

        return when {
            // First reading ever
            lastReadingDate == null -> {
                Log.d(TAG, "First reading ever, starting streak at 1")
                StreakData(currentStreak = 1)
            }

            // Same day reading (don't increment streak)
            lastReadingDate == todayDate -> {
                Log.d(TAG, "Same day reading, maintaining current streak: $currentStreak")
                StreakData(currentStreak = currentStreak)
            }

            // Yesterday's reading (continue streak)
            isYesterday(lastReadingDate, todayDate) -> {
                val newCurrentStreak = currentStreak + 1
                Log.d(TAG, "Yesterday's reading found, continuing streak: $newCurrentStreak")
                StreakData(currentStreak = newCurrentStreak)
            }

            // Streak broken (more than 1 day gap)
            else -> {
                Log.d(TAG, "Streak broken, resetting to 1. Last reading: $lastReadingDate")
                StreakData(currentStreak = 1)
            }
        }
    }

    // Check if lastDate was yesterday compared to todayDate
    private fun isYesterday(lastDateString: String, todayDateString: String): Boolean {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val lastDate = formatter.parse(lastDateString)
            val todayDate = formatter.parse(todayDateString)

            if (lastDate != null && todayDate != null) {
                val calendar = Calendar.getInstance()
                calendar.time = todayDate
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                val yesterdayDate = calendar.time

                val result = formatter.format(yesterdayDate) == lastDateString
                Log.d(
                    TAG,
                    "Yesterday check: $lastDateString vs expected ${formatter.format(yesterdayDate)} = $result"
                )
                result
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing dates for yesterday check", e)
            false
        }
    }

    // Update user journey data manually
    suspend fun updateUserJourney(
        totalReadings: Int,
        currentStreak: Int,
        level: String
    ): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId != null) {
                firebaseRepository.getUserProfile(currentUserId).fold(
                    onSuccess = { user ->
                        if (user != null) {
                            val updatedUser = user.copy(
                                totalReadings = totalReadings,
                                currentStreak = currentStreak,
                                level = level
                            )

                            firebaseRepository.saveUserProfile(updatedUser)
                        } else {
                            Result.failure(Exception("User profile not found"))
                        }
                    },
                    onFailure = { error ->
                        Result.failure(error)
                    }
                )
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Check and update streak based on daily reading completion
    suspend fun checkAndUpdateStreak(): Result<User?> {
        return try {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId != null) {
                // Check if user has done today's reading
                val todaysReading = tarotRepository.getTodaysReading()

                if (todaysReading != null && todaysReading.isRevealed) {
                    // User has completed today's reading, update streak
                    incrementReading()
                } else {
                    // Check if streak should be reset due to missed days
                    firebaseRepository.getUserProfile(currentUserId).fold(
                        onSuccess = { user ->
                            if (user != null) {
                                val todayDate = getTodayDateString()
                                val shouldResetStreak =
                                    shouldResetStreakForMissedDays(user, todayDate)

                                if (shouldResetStreak && user.currentStreak > 0) {
                                    Log.d(TAG, "Resetting streak due to missed days")
                                    val updatedUser = user.copy(currentStreak = 0)

                                    firebaseRepository.saveUserProfile(updatedUser).fold(
                                        onSuccess = { Result.success(updatedUser) },
                                        onFailure = { error -> Result.failure(error) }
                                    )
                                } else {
                                    Result.success(user)
                                }
                            } else {
                                Result.failure(Exception("User profile not found"))
                            }
                        },
                        onFailure = { error -> Result.failure(error) }
                    )
                }
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Check if streak should be reset due to missed days
    private fun shouldResetStreakForMissedDays(user: User, todayDate: String): Boolean {
        val lastReadingDate = user.lastReadingDate ?: return false

        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val lastDate = formatter.parse(lastReadingDate)
            val today = formatter.parse(todayDate)

            if (lastDate != null && today != null) {
                val diffInMillis = today.time - lastDate.time
                val diffInDays = diffInMillis / (24 * 60 * 60 * 1000)

                // Reset streak if more than 1 day has passed
                diffInDays > 1
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking missed days", e)
            false
        }
    }

    // Get current user stats
    suspend fun getUserStats(): UserStats? {
        return try {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId != null) {
                firebaseRepository.getUserProfile(currentUserId).fold(
                    onSuccess = { user ->
                        if (user != null) {
                            UserStats(
                                totalReadings = user.totalReadings,
                                currentStreak = user.currentStreak,
                                level = user.level,
                                experiencePoints = 0 // Keep as 0 for now since it's not in User model
                            )
                        } else {
                            null
                        }
                    },
                    onFailure = { null }
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Convenient method to call when user completes a daily reading
    suspend fun onDailyReadingCompleted(): Result<User?> {
        return incrementReading()
    }

    // Get streak status for UI display
    suspend fun getStreakStatus(): StreakStatus? {
        return try {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId != null) {
                firebaseRepository.getUserProfile(currentUserId).fold(
                    onSuccess = { user ->
                        if (user != null) {
                            val todayDate = getTodayDateString()
                            val isStreakAtRisk = isStreakAtRisk(user, todayDate)
                            val daysSinceLastReading = getDaysSinceLastReading(user, todayDate)

                            StreakStatus(
                                currentStreak = user.currentStreak,
                                isStreakAtRisk = isStreakAtRisk,
                                daysSinceLastReading = daysSinceLastReading,
                                lastReadingDate = user.lastReadingDate
                            )
                        } else {
                            null
                        }
                    },
                    onFailure = { null }
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting streak status", e)
            null
        }
    }

    // Check if streak is at risk (user hasn't read today)
    private fun isStreakAtRisk(user: User, todayDate: String): Boolean {
        val lastReadingDate = user.lastReadingDate ?: return user.currentStreak > 0
        return lastReadingDate != todayDate && user.currentStreak > 0
    }

    // Get days since last reading
    private fun getDaysSinceLastReading(user: User, todayDate: String): Int {
        val lastReadingDate = user.lastReadingDate ?: return 0

        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val lastDate = formatter.parse(lastReadingDate)
            val today = formatter.parse(todayDate)

            if (lastDate != null && today != null) {
                val diffInMillis = today.time - lastDate.time
                (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating days since last reading", e)
            0
        }
    }

    // Helper function to get today's date as string
    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // Data class to hold streak calculation results
    private data class StreakData(
        val currentStreak: Int
    )

    // Data class for streak status information
    data class StreakStatus(
        val currentStreak: Int,
        val isStreakAtRisk: Boolean,
        val daysSinceLastReading: Int,
        val lastReadingDate: String?
    )
}