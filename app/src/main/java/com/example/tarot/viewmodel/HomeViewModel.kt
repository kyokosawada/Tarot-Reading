package com.example.tarot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarot.data.FirebaseRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val dailyInsight: DailyInsight? = null,
    val recentReadings: List<TarotReading> = emptyList(),
    val userStats: UserStats = UserStats(),
    val errorMessage: String? = null
)

data class DailyInsight(
    val message: String,
    val date: String,
    val rating: Int // out of 5
)

data class TarotReading(
    val id: String,
    val type: String,
    val title: String,
    val date: String,
    val cards: List<TarotCard>,
    val interpretation: String
)

data class TarotCard(
    val id: String,
    val name: String,
    val suit: String?,
    val number: Int?,
    val isReversed: Boolean,
    val meaning: String,
    val imageUrl: String? = null
)

data class UserStats(
    val totalReadings: Int = 0,
    val currentStreak: Int = 0,
    val level: String = "Novice",
    val experiencePoints: Int = 0
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val firebaseRepository = FirebaseRepository()
    private val auth = FirebaseAuth.getInstance()

    init {
        loadHomeData()
        observeReadings()
    }

    private fun observeReadings() {
        viewModelScope.launch {
            firebaseRepository.getUserReadings().collectLatest { readings ->
                _uiState.value = _uiState.value.copy(recentReadings = readings)
            }
        }
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                // Simulate API call

                val dailyInsight = generateDailyInsight()
                val userStats = loadUserStats()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    dailyInsight = dailyInsight,
                    userStats = userStats,
                    errorMessage = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    fun startReading(readingType: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val newReading = generateReading(readingType)

                // Save reading to Firestore
                val saveResult = firebaseRepository.saveTarotReading(newReading)
                saveResult.fold(
                    onSuccess = {
                        // Update stats
                        val updatedStats = _uiState.value.userStats.copy(
                            totalReadings = _uiState.value.userStats.totalReadings + 1,
                            experiencePoints = _uiState.value.userStats.experiencePoints + 10
                        )

                        // Save updated stats
                        auth.currentUser?.uid?.let { userId ->
                            firebaseRepository.saveUserStats(userId, updatedStats)
                        }

                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            userStats = updatedStats,
                            errorMessage = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Failed to save reading: ${error.message}"
                        )
                    }
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to start reading: ${e.message}"
                )
            }
        }
    }

    fun refreshDailyInsight() {
        viewModelScope.launch {
            try {
                val newInsight = generateDailyInsight()
                _uiState.value = _uiState.value.copy(dailyInsight = newInsight)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to refresh insight: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun generateDailyInsight(): DailyInsight {
        val insights = listOf(
            "Trust your intuition today. The universe is guiding you toward new opportunities.",
            "Embrace change with an open heart. Today brings unexpected blessings.",
            "Your inner wisdom is stronger than you realize. Listen to what it tells you.",
            "A creative solution to an old problem will present itself today.",
            "The path forward may seem unclear, but have faith in your journey.",
            "Today is perfect for reflection and planning your next moves.",
            "Your compassion will be your greatest strength today."
        )

        return DailyInsight(
            message = insights.random(),
            date = getCurrentDate(),
            rating = (3..5).random()
        )
    }

    private fun generateRecentReadings(): List<TarotReading> {
        return listOf(
            TarotReading(
                id = "reading_1",
                type = "love",
                title = "Love & Relationships Reading",
                date = "2024-01-15",
                cards = generateSampleCards(3),
                interpretation = "Your heart is opening to new possibilities. Trust the process of love."
            ),
            TarotReading(
                id = "reading_2",
                type = "career",
                title = "Career Guidance",
                date = "2024-01-14",
                cards = generateSampleCards(1),
                interpretation = "A new opportunity is approaching. Be ready to take action when it arrives."
            ),
            TarotReading(
                id = "reading_3",
                type = "general",
                title = "General Life Reading",
                date = "2024-01-13",
                cards = generateSampleCards(3),
                interpretation = "Balance is key in all areas of your life. Take time for self-care."
            )
        )
    }

    private suspend fun loadUserStats(): UserStats {
        val userId = auth.currentUser?.uid
        return if (userId != null) {
            firebaseRepository.getUserStats(userId).getOrNull() ?: generateUserStats()
        } else {
            generateUserStats()
        }
    }

    private fun generateUserStats(): UserStats {
        return UserStats(
            totalReadings = 47,
            currentStreak = 12,
            level = "Mystic",
            experiencePoints = 850
        )
    }

    private fun generateReading(type: String): TarotReading {
        val titles = mapOf(
            "love" to "Love & Relationships Reading",
            "career" to "Career & Finance Reading",
            "general" to "General Life Reading",
            "spiritual" to "Spiritual Growth Reading",
            "quick" to "Quick Daily Reading"
        )

        val interpretations = mapOf(
            "love" to "The cards reveal insights about your emotional journey and relationships.",
            "career" to "Your professional path is illuminated with guidance and opportunities.",
            "general" to "The universe offers wisdom for your overall life direction.",
            "spiritual" to "Your spiritual growth is guided by ancient wisdom and inner knowledge.",
            "quick" to "A brief but meaningful glimpse into your day ahead."
        )

        return TarotReading(
            id = "reading_${System.currentTimeMillis()}",
            type = type,
            title = titles[type] ?: "Tarot Reading",
            date = getCurrentDate(),
            cards = generateSampleCards(if (type == "quick") 1 else 3),
            interpretation = interpretations[type]
                ?: "The cards offer guidance for your path forward."
        )
    }

    private fun generateSampleCards(count: Int): List<TarotCard> {
        val sampleCards = listOf(
            TarotCard("1", "The Fool", "Major Arcana", 0, false, "New beginnings, spontaneity, innocence"),
            TarotCard("2", "The Magician", "Major Arcana", 1, false, "Manifestation, resourcefulness, power"),
            TarotCard("3", "The High Priestess", "Major Arcana", 2, false, "Intuition, sacred knowledge, subconscious"),
            TarotCard("4", "The Empress", "Major Arcana", 3, false, "Femininity, beauty, nature, abundance"),
            TarotCard("5", "The Emperor", "Major Arcana", 4, false, "Authority, structure, control, fatherhood"),
            TarotCard("6", "The Hierophant", "Major Arcana", 5, false, "Spiritual wisdom, religious beliefs, conformity"),
            TarotCard("7", "The Lovers", "Major Arcana", 6, false, "Love, harmony, relationships, values alignment"),
            TarotCard("8", "The Chariot", "Major Arcana", 7, false, "Control, willpower, success, determination"),
            TarotCard("9", "Strength", "Major Arcana", 8, false, "Inner strength, bravery, compassion, focus"),
            TarotCard("10", "The Hermit", "Major Arcana", 9, false, "Soul searching, seeking inner guidance, introspection")
        )
        
        return sampleCards.shuffled().take(count)
    }

    private fun getCurrentDate(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
}
