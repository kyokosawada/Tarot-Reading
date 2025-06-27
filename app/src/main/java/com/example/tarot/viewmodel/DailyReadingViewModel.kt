package com.example.tarot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarot.data.model.DailyReading
import com.example.tarot.data.model.TarotCard
import com.example.tarot.data.model.getUprightKeywordsList
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

data class DailyReadingUiState(
    val isLoading: Boolean = false,
    val dailyCard: TarotCard? = null,
    val dailyReading: DailyReading? = null,
    val isCardRevealed: Boolean = false,
    val readingDate: String = "",
    val errorMessage: String? = null,
    val hasDrawnToday: Boolean = false
)

@HiltViewModel
class DailyReadingViewModel @Inject constructor(
    private val tarotRepository: TarotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DailyReadingUiState())
    val uiState: StateFlow<DailyReadingUiState> = _uiState.asStateFlow()

    init {
        checkDailyReading()
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
                        hasDrawnToday = true
                    )
                } else {
                    // No reading for today, draw a new card
                    drawDailyCard()
                }

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

                println("DailyReadingViewModel: Drawing new daily card")

                // Get random card from database
                val randomCard = tarotRepository.getRandomCard()

                // Save daily reading to database
                val dailyReading = tarotRepository.saveDailyReading(randomCard)

                println("DailyReadingViewModel: Successfully drew and saved card: ${randomCard.name}")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    dailyCard = randomCard,
                    dailyReading = dailyReading,
                    isCardRevealed = false,
                    readingDate = getCurrentDate(),
                    hasDrawnToday = true,
                    errorMessage = null
                )

                // Clean up old readings (keep last 30 days)
                tarotRepository.cleanupOldReadings(30)

            } catch (e: Exception) {
                println("DailyReadingViewModel: Error drawing card: ${e.message}")
                e.printStackTrace()
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

                    _uiState.value = _uiState.value.copy(
                        isCardRevealed = true,
                        dailyReading = updatedReading
                    )
                } else {
                    // Fallback for local state update
                    _uiState.value = _uiState.value.copy(isCardRevealed = true)
                }
            } catch (e: Exception) {
                // If database update fails, still update UI
                _uiState.value = _uiState.value.copy(isCardRevealed = true)
                println("Failed to update reading reveal status: ${e.message}")
            }
        }
    }

    fun resetDailyReading() {
        _uiState.value = DailyReadingUiState(readingDate = getCurrentDate())
        checkDailyReading()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())
    }

    // Helper function to format card keywords
    fun getFormattedKeywords(card: TarotCard): String {
        return card.getUprightKeywordsList().joinToString(" • ")
    }
}