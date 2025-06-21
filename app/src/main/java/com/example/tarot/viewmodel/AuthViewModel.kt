package com.example.tarot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarot.data.DummyAuthRepository
import com.example.tarot.data.LoginResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val user: User? = null,
    val signupSuccess: Boolean = false,
    val successMessage: String? = null
)

data class User(
    val id: String,
    val name: String,
    val email: String
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Dummy data repository for testing
    private val authRepository = DummyAuthRepository()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                // Basic validation
                if (email.isBlank() || password.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please fill in all fields"
                    )
                    return@launch
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please enter a valid email address"
                    )
                    return@launch
                }

                // Use dummy data repository for login
                when (val result = authRepository.login(email, password)) {
                    is LoginResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            user = result.user,
                            errorMessage = null
                        )
                    }

                    is LoginResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Login failed: ${e.message}"
                )
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                // Basic validation
                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please fill in all fields"
                    )
                    return@launch
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please enter a valid email address"
                    )
                    return@launch
                }

                if (password.length < 6) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Password must be at least 6 characters"
                    )
                    return@launch
                }

                // Use dummy data repository for signup
                when (val result = authRepository.signUp(name, email, password)) {
                    is LoginResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            signupSuccess = true,
                            successMessage = "Sign up successful! Please log in.",
                            user = result.user,
                            errorMessage = null
                        )
                    }

                    is LoginResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Sign up failed: ${e.message}"
                )
            }
        }
    }

    fun logout() {
        _uiState.value = AuthUiState() // Reset to initial state
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
