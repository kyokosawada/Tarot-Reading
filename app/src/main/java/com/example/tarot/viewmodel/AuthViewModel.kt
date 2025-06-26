package com.example.tarot.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tarot.data.DummyAuthRepository
import com.example.tarot.data.FirebaseRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
    val user: User? = null,
    val signupSuccess: Boolean = false,
    val successMessage: String? = null,
    val needsProfileCompletion: Boolean = false
)

data class User(
    val id: String,
    val name: String,
    val email: String,
    val username: String? = null,  // Preferred username for the app
    val birthMonth: Int? = null,  // 1-12 for January-December
    val birthYear: Int? = null,
    val isProfileComplete: Boolean = false,
    val createdAt: Long? = null  // Timestamp when user joined
)

class AuthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Firebase Auth instance
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Firebase repository for Firestore operations
    private val firebaseRepository = FirebaseRepository()

    // Dummy data repository for testing
    private val authRepository = DummyAuthRepository()

    // Web client ID from Firebase Console
    private val webClientId =
        "972003711031-4o27g7sjiet4kqq1l12j15ig0ji36ik3.apps.googleusercontent.com"

    fun completeProfile(username: String, birthMonth: Int, birthYear: Int) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val currentUser = _uiState.value.user
                if (currentUser != null) {
                    // Validate username
                    if (username.isBlank()) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Please enter a username"
                        )
                        return@launch
                    }

                    if (username.length < 3) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Username must be at least 3 characters"
                        )
                        return@launch
                    }

                    if (username.length > 20) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Username must be 20 characters or less"
                        )
                        return@launch
                    }

                    // Validate username contains only valid characters
                    if (!username.matches(Regex("^[a-zA-Z0-9_]+$"))) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Username can only contain letters, numbers, and underscores"
                        )
                        return@launch
                    }

                    // Validate birth date
                    if (birthMonth < 1 || birthMonth > 12) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Please select a valid birth month"
                        )
                        return@launch
                    }

                    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    if (birthYear < 1900 || birthYear > currentYear) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Please enter a valid birth year"
                        )
                        return@launch
                    }

                    // Update user with profile completion information
                    val updatedUser = currentUser.copy(
                        username = username,
                        birthMonth = birthMonth,
                        birthYear = birthYear,
                        isProfileComplete = true
                    )

                    // Save to Firestore database
                    val result = firebaseRepository.saveUserProfile(updatedUser)
                    result.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                user = updatedUser,
                                needsProfileCompletion = false,
                                successMessage = "Profile completed successfully!"
                            )
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Failed to save profile: ${error.message}"
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "User not found. Please log in again."
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to complete profile: ${e.message}"
                )
            }
        }
    }

    init {
        // Check if user is already logged in
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Load user profile from Firestore
            viewModelScope.launch {
                val profileResult = firebaseRepository.getUserProfile(currentUser.uid)
                profileResult.fold(
                    onSuccess = { firestoreUser ->
                        val user = firestoreUser ?: User(
                            id = currentUser.uid,
                            name = currentUser.displayName ?: "",
                            email = currentUser.email ?: "",
                            username = null,
                            birthMonth = null,
                            birthYear = null,
                            isProfileComplete = false,
                            createdAt = System.currentTimeMillis()
                        )

                        _uiState.value = _uiState.value.copy(
                            isLoggedIn = true,
                            user = user,
                            needsProfileCompletion = !user.isProfileComplete
                        )
                    },
                    onFailure = {
                        // Fallback to Firebase Auth data if Firestore fails
                        val user = User(
                            id = currentUser.uid,
                            name = currentUser.displayName ?: "",
                            email = currentUser.email ?: "",
                            username = null,
                            birthMonth = null,
                            birthYear = null,
                            isProfileComplete = false
                        )

                        _uiState.value = _uiState.value.copy(
                            isLoggedIn = true,
                            user = user,
                            needsProfileCompletion = true
                        )
                    }
                )
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )

                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                // Sign in to Firebase with Google ID token
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()

                val firebaseUser = authResult.user
                if (firebaseUser != null) {
                    // Load user profile from Firestore to check if it's already complete
                    val profileResult = firebaseRepository.getUserProfile(firebaseUser.uid)
                    profileResult.fold(
                        onSuccess = { firestoreUser ->
                            val user = firestoreUser ?: User(
                                id = firebaseUser.uid,
                                name = firebaseUser.displayName ?: "",
                                email = firebaseUser.email ?: "",
                                username = null, // Will be set during profile completion
                                birthMonth = null, // Will be set during profile completion
                                birthYear = null,
                                isProfileComplete = false, // New Google users need to complete profile
                                createdAt = System.currentTimeMillis()
                            )

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                user = user,
                                needsProfileCompletion = !user.isProfileComplete,
                                errorMessage = null
                            )
                        },
                        onFailure = {
                            // Fallback to Firebase Auth data if Firestore fails
                            val user = User(
                                id = firebaseUser.uid,
                                name = firebaseUser.displayName ?: "",
                                email = firebaseUser.email ?: "",
                                username = null,
                                birthMonth = null,
                                birthYear = null,
                                isProfileComplete = false,
                                createdAt = System.currentTimeMillis()
                            )

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                user = user,
                                needsProfileCompletion = true,
                                errorMessage = null
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Google sign-in failed"
                    )
                }

            } catch (e: GetCredentialException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google sign-in cancelled or failed: ${e.message}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google sign-in failed: ${e.message}"
                )
            }
        }
    }

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

                // Firebase Email/Password Sign In
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    // Load user profile from Firestore
                    val profileResult = firebaseRepository.getUserProfile(firebaseUser.uid)
                    profileResult.fold(
                        onSuccess = { firestoreUser ->
                            val user = firestoreUser ?: User(
                                id = firebaseUser.uid,
                                name = firebaseUser.displayName ?: "",
                                email = firebaseUser.email ?: "",
                                username = null,
                                birthMonth = null,
                                birthYear = null,
                                isProfileComplete = false,
                                createdAt = System.currentTimeMillis()
                            )

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                user = user,
                                needsProfileCompletion = !user.isProfileComplete,
                                errorMessage = null
                            )
                        },
                        onFailure = {
                            // Fallback to Firebase Auth data if Firestore fails
                            val user = User(
                                id = firebaseUser.uid,
                                name = firebaseUser.displayName ?: "",
                                email = firebaseUser.email ?: "",
                                username = null,
                                birthMonth = null,
                                birthYear = null,
                                isProfileComplete = false,
                                createdAt = System.currentTimeMillis()
                            )

                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                user = user,
                                needsProfileCompletion = true,
                                errorMessage = null
                            )
                        }
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Login failed"
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Login failed: ${e.message}"
                )
            }
        }
    }

    fun signUp(
        name: String,
        email: String,
        password: String,
        birthMonth: Int? = null,
        birthYear: Int? = null
    ) {
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

                // Firebase Email/Password Sign Up
                val authResult =
                    firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    // Update user profile with name
                    val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                        displayName = name
                    }
                    firebaseUser.updateProfile(profileUpdates).await()

                    val isProfileComplete = birthMonth != null && birthYear != null

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        signupSuccess = true,
                        successMessage = "Sign up successful! Please log in.",
                        user = User(
                            id = firebaseUser.uid,
                            name = name,
                            email = firebaseUser.email ?: "",
                            username = null,
                            birthMonth = birthMonth,
                            birthYear = birthYear,
                            isProfileComplete = isProfileComplete,
                            createdAt = System.currentTimeMillis()
                        ),
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Sign up failed"
                    )
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
        viewModelScope.launch {
            try {
                firebaseAuth.signOut()
                _uiState.value = AuthUiState() // Reset to initial state
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Logout failed: ${e.message}"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}
