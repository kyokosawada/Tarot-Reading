package com.example.tarot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarot.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val allowReversedCards: Boolean = true,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Load settings on initialization
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.allowReversedCards.collect { allowReversed ->
                _uiState.value = _uiState.value.copy(
                    allowReversedCards = allowReversed,
                    error = null
                )
            }
        }
    }

    fun toggleReversedCards() {
        viewModelScope.launch {
            val newValue = !_uiState.value.allowReversedCards

            // Optimistic update - immediately update UI
            _uiState.value = _uiState.value.copy(
                allowReversedCards = newValue,
                error = null
            )

            try {
                settingsRepository.setAllowReversedCards(newValue)
            } catch (e: Exception) {
                // Revert optimistic update on error
                _uiState.value = _uiState.value.copy(
                    allowReversedCards = !newValue,
                    error = "Failed to update setting"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }


}