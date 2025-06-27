package com.example.tarot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
                // For now, we'll generate a new card each time
                // Later you can add logic to check if user already drew today

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    readingDate = currentDate,
                    hasDrawnToday = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to check daily reading: ${e.message}"
                )
            }
        }
    }

    fun drawDailyCard() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                println("DailyReadingViewModel: Starting to draw daily card")

                // Get random card from Room database
                val randomCard = tarotRepository.getRandomCard()

                println("DailyReadingViewModel: Successfully drew card: ${randomCard.name}")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    dailyCard = randomCard,
                    hasDrawnToday = true,
                    errorMessage = null
                )

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
        _uiState.value = _uiState.value.copy(isCardRevealed = true)
    }

    fun resetDailyReading() {
        _uiState.value = DailyReadingUiState(readingDate = getCurrentDate())
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