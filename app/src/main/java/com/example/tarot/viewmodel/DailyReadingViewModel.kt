package com.example.tarot.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarot.data.model.DailyReading
import com.example.tarot.data.model.TarotCard
import com.example.tarot.data.model.getReversedKeywordsList
import com.example.tarot.data.model.getUprightKeywordsList
import com.example.tarot.data.repository.JourneyRepository
import com.example.tarot.data.repository.OpenAiRepository
import com.example.tarot.data.repository.SettingsRepository
import com.example.tarot.data.repository.TarotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    val streakStatus: JourneyRepository.StreakStatus? = null, // Add streak tracking
    val aiGeneratedMessage: String? = null,
    val isAiLoading: Boolean = false,
    val aiError: String? = null,
    val isProcessingReveal: Boolean = false, // Prevent spam clicking
    val isGuidanceReady: Boolean = false // Track if guidance is ready to show card
)

class DailyReadingViewModel(
    private val tarotRepository: TarotRepository,
    private val settingsRepository: SettingsRepository,
    private val journeyRepository: JourneyRepository,
    private val openAiRepository: OpenAiRepository,
    private val firebaseRepository: com.example.tarot.data.FirebaseRepository
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

    private fun generatePersonalizedDailyMessage() {
        val card = _uiState.value.dailyCard ?: return
        val isReversed = _uiState.value.isReversed
        val dailyReading = _uiState.value.dailyReading

        viewModelScope.launch {
            try {
                // Check if AI guidance already exists in the database
                if (dailyReading?.aiGuidance != null) {
                    Log.d(
                        "DailyReadingViewModel",
                        "AI guidance already exists, using cached version"
                    )
                    _uiState.value = _uiState.value.copy(
                        aiGeneratedMessage = dailyReading.aiGuidance,
                        isAiLoading = false,
                        aiError = null,
                        isGuidanceReady = true
                    )
                    return@launch
                }

                // No cached guidance, generate new one
                Log.d("DailyReadingViewModel", "No cached AI guidance, generating new one")

                _uiState.value = _uiState.value.copy(
                    isAiLoading = true,
                    aiError = null
                )

                // Create a daily reading question for the AI
                val dailyQuestion = "What guidance does this card offer for today?"

                val result = openAiRepository.getPersonalizedTarotReading(
                    question = dailyQuestion,
                    allowReversed = _uiState.value.allowReversedCards
                )

                result.fold(
                    onSuccess = { tarotReading ->
                        val guidance = tarotReading.personalizedGuidance

                        // Save the guidance to database for future use
                        val currentDate = getCurrentDateForDatabase()
                        tarotRepository.updateDailyReadingWithAiGuidance(currentDate, guidance)

                        // Update the local state with the new guidance
                        val updatedReading = dailyReading?.copy(aiGuidance = guidance)

                        _uiState.value = _uiState.value.copy(
                            aiGeneratedMessage = guidance,
                            isAiLoading = false,
                            aiError = null,
                            isGuidanceReady = true,
                            dailyReading = updatedReading
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isAiLoading = false,
                            aiError = exception.message
                                ?: "Failed to generate personalized guidance",
                            isGuidanceReady = true
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAiLoading = false,
                    aiError = "An error occurred while generating personalized guidance",
                    isGuidanceReady = true
                )
                Log.e("DailyReadingViewModel", "Error generating personalized message", e)
            }
        }
    }

    fun retryAiGeneration() {
        generatePersonalizedDailyMessage()
    }

    fun clearAiError() {
        _uiState.value = _uiState.value.copy(aiError = null)
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
                        isReversed = existingReading.isReversed,
                        isGuidanceReady = existingReading.aiGuidance != null,
                        aiGeneratedMessage = existingReading.aiGuidance
                    )

                    // Always generate personalized message for existing reading
                    generatePersonalizedDailyMessage()
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

                // Start generating AI guidance immediately so it's ready when user taps
                generatePersonalizedDailyMessage()

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
                // Prevent spam clicking
                if (_uiState.value.isProcessingReveal) {
                    Log.d(
                        "DailyReadingViewModel",
                        "Card reveal already in progress, ignoring click"
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(isProcessingReveal = true)

                val currentReading = _uiState.value.dailyReading
                if (currentReading != null && !currentReading.isRevealed) {
                    // Update reading as revealed in database
                    val updatedReading = currentReading.copy(isRevealed = true)
                    tarotRepository.updateDailyReading(updatedReading)

                    // Save to Firebase
                    val card = _uiState.value.dailyCard
                    if (card != null) {
                        viewModelScope.launch {
                            try {
                                val tarotReading = com.example.tarot.viewmodel.TarotReading(
                                    id = "daily_${System.currentTimeMillis()}",
                                    type = "daily",
                                    title = "Daily Reading",
                                    date = getCurrentDateForDatabase(),
                                    cards = listOf(card),
                                    interpretation = _uiState.value.aiGeneratedMessage
                                        ?: (if (_uiState.value.isReversed) card.reversedDailyMessage else card.uprightDailyMessage),
                                    journalNotes = ""
                                )

                                val saveResult = firebaseRepository.saveTarotReading(tarotReading)
                                saveResult.fold(
                                    onSuccess = {
                                        android.util.Log.d(
                                            "DailyReadingViewModel",
                                            "Daily reading saved successfully"
                                        )
                                    },
                                    onFailure = { error ->
                                        android.util.Log.e(
                                            "DailyReadingViewModel",
                                            "Failed to save daily reading: ${error.message}"
                                        )
                                    }
                                )
                            } catch (e: Exception) {
                                android.util.Log.e(
                                    "DailyReadingViewModel",
                                    "Error saving daily reading: ${e.message}"
                                )
                            }
                        }
                    }

                    // Update streak tracking (only on first reveal)
                    journeyRepository.onDailyReadingCompleted()

                    // Get updated streak status
                    val streakStatus = journeyRepository.getStreakStatus()

                    _uiState.value = _uiState.value.copy(
                        isCardRevealed = true,
                        dailyReading = updatedReading,
                        streakStatus = streakStatus,
                        isProcessingReveal = false
                    )

                    // Guidance is already pre-loaded, no need to generate again
                } else {
                    // Fallback for local state update
                    _uiState.value = _uiState.value.copy(
                        isCardRevealed = true,
                        isProcessingReveal = false
                    )
                    // Guidance is already pre-loaded, no need to generate again
                }
            } catch (e: Exception) {
                // If database update fails, still update UI
                _uiState.value = _uiState.value.copy(
                    isCardRevealed = true,
                    isProcessingReveal = false
                )
                // Guidance is already pre-loaded, no need to generate again
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

    // Helper: get current date in database-friendly format for querying today (yyyy-MM-dd)
    private fun getCurrentDateForDatabase(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
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