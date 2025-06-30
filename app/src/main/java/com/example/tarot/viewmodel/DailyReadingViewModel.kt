package com.example.tarot.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarot.data.model.DailyReading
import com.example.tarot.data.model.TarotCard
import com.example.tarot.data.model.getReversedKeywordsList
import com.example.tarot.data.model.getUprightKeywordsList
import com.example.tarot.data.repository.JourneyRepository
import com.example.tarot.data.repository.SettingsRepository
import com.example.tarot.data.repository.TarotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random

data class DailyReadingUiState(
    val isLoading: Boolean = false,
    val dailyCard: TarotCard? = null,
    val dailyReading: DailyReading? = null,
    val isCardRevealed: Boolean = false,
    val readingDate: String = "",
    val errorMessage: String? = null,
    val hasDrawnToday: Boolean = false,
    val isReversed: Boolean = false,
    val allowReversedCards: Boolean = false, // Will be set from settings
    val streakStatus: JourneyRepository.StreakStatus? = null // Add streak tracking
)

@HiltViewModel
class DailyReadingViewModel @Inject constructor(
    private val tarotRepository: TarotRepository,
    private val settingsRepository: SettingsRepository,
    private val journeyRepository: JourneyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyReadingUiState())
    val uiState: StateFlow<DailyReadingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.allowReversedCards.collect { allowReversed ->
                _uiState.value = _uiState.value.copy(allowReversedCards = allowReversed)
            }
        }
        checkDailyReading()
        loadStreakStatus()
    }

    private fun checkDailyReading() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val currentDate = getCurrentDate()
                val existingReading = tarotRepository.getTodaysReading()

                if (existingReading != null) {
                    // User already has a reading for today
                    val card = tarotRepository.getCardById(existingReading.cardId)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        dailyCard = card,
                        dailyReading = existingReading,
                        isCardRevealed = existingReading.isRevealed,
                        readingDate = currentDate,
                        hasDrawnToday = true,
                        isReversed = existingReading.isReversed
                    )
                } else {
                    // No reading for today, draw a new card
                    drawDailyCard()
                }

                // Load streak status after checking/setting the reading
                loadStreakStatus()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to check daily reading: ${e.message}"
                )
            }
        }
    }

    private fun drawDailyCard() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                Log.d("DailyReadingViewModel", "Drawing new daily card")

                // Get random card from database
                val randomCard = tarotRepository.getRandomCard()

                // Randomly determine if card is reversed (50/50 chance) when allowed by settings
                val allowReversed = settingsRepository.getCurrentAllowReversedCards()
                val isReversed = allowReversed && Random.nextBoolean()

                Log.d("DailyReadingViewModel", "Allow reversed cards: $allowReversed")
                Log.d("DailyReadingViewModel", "Card is reversed: $isReversed")

                // Save daily reading to database with orientation
                val dailyReading = tarotRepository.saveDailyReading(randomCard, isReversed)

                Log.d(
                    "DailyReadingViewModel",
                    "Successfully drew and saved card: ${randomCard.name} - ${if (isReversed) "Reversed" else "Upright"}"
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    dailyCard = randomCard,
                    dailyReading = dailyReading,
                    isCardRevealed = false,
                    readingDate = getCurrentDate(),
                    hasDrawnToday = true,
                    isReversed = isReversed,
                    errorMessage = null
                )

                // Clean up old readings (keep last 30 days)
                tarotRepository.cleanupOldReadings(30)

            } catch (e: Exception) {
                Log.e("DailyReadingViewModel", "Error drawing card", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to draw daily card: ${e.message}"
                )
            }
        }
    }

    fun revealCard() {
        viewModelScope.launch {
            try {
                val currentReading = _uiState.value.dailyReading
                if (currentReading != null && !currentReading.isRevealed) {
                    // Update reading as revealed in database
                    val updatedReading = currentReading.copy(isRevealed = true)
                    tarotRepository.updateDailyReading(updatedReading)

                    // Update streak tracking (only on first reveal)
                    journeyRepository.onDailyReadingCompleted()

                    // Get updated streak status
                    val streakStatus = journeyRepository.getStreakStatus()

                    _uiState.value = _uiState.value.copy(
                        isCardRevealed = true,
                        dailyReading = updatedReading,
                        streakStatus = streakStatus
                    )
                } else {
                    // Fallback for local state update
                    _uiState.value = _uiState.value.copy(isCardRevealed = true)
                }
            } catch (e: Exception) {
                // If database update fails, still update UI
                _uiState.value = _uiState.value.copy(isCardRevealed = true)
                Log.e("DailyReadingViewModel", "Failed to update reading reveal status", e)
            }
        }
    }

    fun resetDailyReading() {
        _uiState.value = DailyReadingUiState(readingDate = getCurrentDate())
        checkDailyReading()
    }

    private fun loadStreakStatus() {
        viewModelScope.launch {
            try {
                val streakStatus = journeyRepository.getStreakStatus()
                _uiState.value = _uiState.value.copy(streakStatus = streakStatus)
            } catch (e: Exception) {
                Log.e("DailyReadingViewModel", "Failed to load streak status", e)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun toggleReversedCards() {
        viewModelScope.launch {
            val current = settingsRepository.getCurrentAllowReversedCards()
            settingsRepository.setAllowReversedCards(!current)
        }
    }

    fun setAllowReversedCards(allow: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAllowReversedCards(allow)
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
    }

    // Helper function to format card keywords
    fun getFormattedKeywords(card: TarotCard, isReversed: Boolean): String {
        return if (isReversed) {
            card.getReversedKeywordsList().joinToString(" • ")
        } else {
            card.getUprightKeywordsList().joinToString(" • ")
        }
    }

    // Helper function to get 3 random keywords for daily reading
    fun getRandomKeywords(card: TarotCard, isReversed: Boolean): List<String> {
        val allKeywords = if (isReversed) {
            card.getReversedKeywordsList()
        } else {
            card.getUprightKeywordsList()
        }
        return if (allKeywords.size <= 3) {
            allKeywords
        } else {
            // Use consistent seed based on card ID and current date to ensure same keywords each time
            val currentDate = getCurrentDate()
            val seed = (card.id.toString() + currentDate).hashCode()
            allKeywords.shuffled(kotlin.random.Random(seed)).take(3)
        }
    }
}