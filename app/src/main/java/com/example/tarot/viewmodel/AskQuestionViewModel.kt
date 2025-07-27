package com.example.tarot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarot.data.FirebaseRepository
import com.example.tarot.data.model.TarotReadingResponse
import com.example.tarot.data.repository.JourneyRepository
import com.example.tarot.data.repository.OpenAiRepository
import com.example.tarot.data.repository.SettingsRepository
import com.example.tarot.util.ApiKeyManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AskQuestionViewModel(
    private val openAiRepository: OpenAiRepository,
    private val settingsRepository: SettingsRepository,
    private val journeyRepository: JourneyRepository,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private val _uiState = MutableStateFlow(AskQuestionUiState())
    val uiState: StateFlow<AskQuestionUiState> = _uiState.asStateFlow()

    private var lastQuestion: String = "" // Store last question for retry

    fun askQuestion(question: String, apiKey: String? = null) {
        if (question.isBlank()) return

        lastQuestion = question // Store for retry

        android.util.Log.d("AskQuestionViewModel", "🚀 Starting askQuestion with: $question")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            // Get current settings
            val allowReversed = settingsRepository.getCurrentAllowReversedCards()

            android.util.Log.d("AskQuestionViewModel", "🔄 Calling OpenAI API...")

            val result = openAiRepository.getPersonalizedTarotReading(
                question = question,
                apiKey = apiKey ?: ApiKeyManager.getOpenAiApiKey(),
                allowReversed = allowReversed
            )

            result.fold(
                onSuccess = { reading ->
                    // Save reading to Firebase (remove nested coroutine)
                    try {
                        val tarotReading = com.example.tarot.viewmodel.TarotReading(
                            id = "question_${System.currentTimeMillis()}",
                            type = "question",
                            title = "Question Reading",
                            date = getCurrentDate(),
                            cards = listOf(reading.tarotCard),
                            interpretation = reading.personalizedGuidance,
                            journalNotes = ""
                        )

                        android.util.Log.d(
                            "AskQuestionViewModel",
                            "🔄 Attempting to save reading: ${tarotReading.id}"
                        )

                        // Save to Firebase directly (already in coroutine scope)
                        val saveResult = firebaseRepository.saveTarotReading(tarotReading)
                        saveResult.fold(
                            onSuccess = {
                                android.util.Log.d(
                                    "AskQuestionViewModel",
                                    "✅ Reading saved successfully: ${tarotReading.id}"
                                )
                            },
                            onFailure = { error ->
                                android.util.Log.e(
                                    "AskQuestionViewModel",
                                    "❌ Failed to save reading: ${error.message}"
                                )
                            }
                        )
                    } catch (e: Exception) {
                        android.util.Log.e(
                            "AskQuestionViewModel",
                            "❌ Error saving reading: ${e.message}"
                        )
                    }

                    // Increment reading journey metric on successful reading
                    journeyRepository.incrementReading()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        reading = reading,
                        hasAskedQuestion = true,
                        error = null
                    )
                },
                onFailure = { exception ->
                    android.util.Log.e(
                        "AskQuestionViewModel",
                        "❌ OpenAI API call failed: ${exception.message}"
                    )
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "An error occurred",
                        hasAskedQuestion = false
                    )
                }
            )
        }
    }

    fun resetReading() {
        _uiState.value = AskQuestionUiState()
        lastQuestion = ""
    }

    fun setCardRevealed(revealed: Boolean) {
        _uiState.value = _uiState.value.copy(isCardRevealed = revealed)
    }

    fun setShowInterpretation(show: Boolean) {
        _uiState.value = _uiState.value.copy(showInterpretation = show)
    }

    fun retryLastQuestion() {
        if (lastQuestion.isNotBlank()) {
            askQuestion(lastQuestion)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
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
}

data class AskQuestionUiState(
    val isLoading: Boolean = false,
    val reading: TarotReadingResponse? = null,
    val hasAskedQuestion: Boolean = false,
    val isCardRevealed: Boolean = false,
    val showInterpretation: Boolean = false,
    val error: String? = null
)