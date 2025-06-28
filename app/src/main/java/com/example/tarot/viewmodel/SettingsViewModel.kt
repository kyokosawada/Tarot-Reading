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
    val isLoading: Boolean = false
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
                    isLoading = false
                )
            }
        }
    }

    fun toggleReversedCards() {
        viewModelScope.launch {
            val newValue = !_uiState.value.allowReversedCards
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                settingsRepository.setAllowReversedCards(newValue)
                // State will be updated automatically through the Flow
            } catch (e: Exception) {
                // Handle error - revert loading state
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }


}