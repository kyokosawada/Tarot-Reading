package com.example.tarot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarot.data.FirebaseRepository
import com.example.tarot.data.model.TarotCard
import com.example.tarot.data.repository.JourneyRepository
import com.example.tarot.data.repository.TarotRepository
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
    val interpretation: String,
    val journalNotes: String = "" // Add journal support
)



data class UserStats(
    val totalReadings: Int = 0,
    val currentStreak: Int = 0,
    val level: String = "Novice",
    val experiencePoints: Int = 0
)

class HomeViewModel(
    private val firebaseRepository: FirebaseRepository,
    private val tarotRepository: TarotRepository,
    private val journeyRepository: JourneyRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
        observeReadings()
    }

    // Add method to refresh user stats (called externally after auth refresh)
    fun refreshUserStats() {
        viewModelScope.launch {
            try {
                // Refresh user stats from journey repository
                val userStats = loadUserStats()
                _uiState.value = _uiState.value.copy(userStats = userStats)
            } catch (e: Exception) {
                // Log error but don't show to user as it's background refresh
                android.util.Log.e("HomeViewModel", "Failed to refresh user stats", e)
            }
        }
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
                        // Increment reading journey metric
                        viewModelScope.launch {
                            journeyRepository.incrementReading()
                        }
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
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
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                val newInsight = generateDailyInsight()
                _uiState.value = _uiState.value.copy(
                    dailyInsight = newInsight,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to refresh insight: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun updateJournalNotes(readingId: String, notes: String) {
        viewModelScope.launch {
            try {
                val result = firebaseRepository.updateJournalNotes(readingId, notes)
                result.fold(
                    onSuccess = {
                        // Update the reading in the current state
                        val updatedReadings = _uiState.value.recentReadings.map { reading ->
                            if (reading.id == readingId) {
                                reading.copy(journalNotes = notes)
                            } else {
                                reading
                            }
                        }
                        _uiState.value = _uiState.value.copy(recentReadings = updatedReadings)
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Failed to update journal: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error updating journal: ${e.message}"
                )
            }
        }
    }

    private suspend fun generateDailyInsight(): DailyInsight {
        // Get a random tarot card for daily insight
        val randomCard = tarotRepository.getRandomCard()
        val isReversed =
            kotlin.random.Random.nextBoolean() // Randomly determine if card is reversed

        return DailyInsight(
            message = if (isReversed) randomCard.reversedDailyMessage else randomCard.uprightDailyMessage,
            date = getCurrentDate(),
            rating = (3..5).random()
        )
    }



    private suspend fun loadUserStats(): UserStats {
        val userStats = journeyRepository.getUserStats()
        return userStats ?: UserStats()
    }


    private suspend fun generateReading(type: String): TarotReading {
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

        // Get real cards from Room database
        val cardCount = if (type == "quick") 1 else 3
        val cards = tarotRepository.getRandomCards(cardCount)

        return TarotReading(
            id = "reading_${System.currentTimeMillis()}",
            type = type,
            title = titles[type] ?: "Tarot Reading",
            date = getCurrentDate(),
            cards = cards,
            interpretation = interpretations[type]
                ?: "The cards offer guidance for your path forward."
        )
    }



    private fun getCurrentDate(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
    }
}
