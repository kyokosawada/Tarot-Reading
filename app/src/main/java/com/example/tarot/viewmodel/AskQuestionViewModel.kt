package com.example.tarot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarot.data.model.TarotReadingResponse
import com.example.tarot.data.repository.OpenAiRepository
import com.example.tarot.util.ApiKeyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AskQuestionViewModel @Inject constructor(
    private val openAiRepository: OpenAiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AskQuestionUiState())
    val uiState: StateFlow<AskQuestionUiState> = _uiState.asStateFlow()

    fun askQuestion(question: String, apiKey: String? = null) {
        if (question.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val result = openAiRepository.getPersonalizedTarotReading(
                question = question,
                apiKey = apiKey ?: ApiKeyManager.getOpenAiApiKey()
            )

            result.fold(
                onSuccess = { reading ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        reading = reading,
                        hasAskedQuestion = true,
                        error = null
                    )
                },
                onFailure = { exception ->
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
    }

    fun setCardRevealed(revealed: Boolean) {
        _uiState.value = _uiState.value.copy(isCardRevealed = revealed)
    }

    fun setShowInterpretation(show: Boolean) {
        _uiState.value = _uiState.value.copy(showInterpretation = show)
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