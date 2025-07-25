package com.example.tarot.viewmodel

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isInitializing: Boolean = true, // New: Track initial auth check
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
    val createdAt: Long? = null,  // Timestamp when user joined
    // Journey data fields
    val totalReadings: Int = 0,
    val currentStreak: Int = 0,
    val lastReadingDate: String? = null, // Format: "yyyy-MM-dd" for streak calculation
    val level: String = "Novice"
)

class AuthViewModel(
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"

        // Calculate user level based on total readings
        fun calculateUserLevel(totalReadings: Int): String {
            return when {
                totalReadings >= 100 -> "Master"
                totalReadings >= 50 -> "Mystic"
                totalReadings >= 20 -> "Seeker"
                totalReadings >= 5 -> "Apprentice"
                else -> "Novice"
            }
        }
    }
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Firebase Auth instance
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Web client ID from Firebase Console
    private val webClientId =
        "972003711031-4o27g7sjiet4kqq1l12j15ig0ji36ik3.apps.googleusercontent.com"

    // Auth state listener to monitor token persistence
    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        Log.d(TAG, "Firebase AuthStateListener triggered")
        Log.d(TAG, "Current user from listener: ${auth.currentUser?.uid}")
        Log.d(TAG, "Current user email: ${auth.currentUser?.email}")
        Log.d(TAG, "Current user display name: ${auth.currentUser?.displayName}")

        if (auth.currentUser != null) {
            Log.d(TAG, "User is authenticated via AuthStateListener")
            viewModelScope.launch {
                loadUserProfileFromFirestore(auth.currentUser!!)
            }
        } else {
            Log.d(TAG, "No user authenticated via AuthStateListener")
            _uiState.value = _uiState.value.copy(
                isLoggedIn = false,
                user = null,
                needsProfileCompletion = false
            )
        }
    }

    init {
        Log.d(TAG, "AuthViewModel initializing...")

        // Add auth state listener first
        firebaseAuth.addAuthStateListener(authStateListener)
        Log.d(TAG, "AuthStateListener added")

        // Check if user is already logged in
        viewModelScope.launch {
            checkCurrentUser()
            _uiState.value = _uiState.value.copy(isInitializing = false)
        }
    }

    // Clear Firebase Auth cache to resolve encryption issues
    fun clearAuthCache() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Clearing Firebase Auth cache due to encryption issues")
                firebaseAuth.signOut()
                _uiState.value = AuthUiState(
                    isLoading = false,
                    isInitializing = false,
                    isLoggedIn = false,
                    errorMessage = null,
                    user = null,
                    signupSuccess = false,
                    successMessage = null,
                    needsProfileCompletion = false
                )
                Log.d(TAG, "Firebase Auth cache cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing auth cache: ${e.message}", e)
            }
        }
    }

    private suspend fun checkCurrentUser() {
        Log.d(TAG, "Checking current user for persistent login")
        val currentUser = firebaseAuth.currentUser
        Log.d(TAG, "Firebase currentUser: ${currentUser?.uid}")

        if (currentUser != null) {
            Log.d(TAG, "User is authenticated, loading profile from Firestore")
            loadUserProfileFromFirestore(currentUser)
        } else {
            Log.d(TAG, "No current user found, setting as logged out")
            // User is not logged in
            _uiState.value = _uiState.value.copy(
                isLoggedIn = false,
                user = null,
                needsProfileCompletion = false
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "AuthViewModel clearing, removing AuthStateListener")
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    private suspend fun loadUserProfileFromFirestore(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        Log.d(TAG, "Loading user profile from Firestore for: ${firebaseUser.uid}")
        val profileResult = firebaseRepository.getUserProfile(firebaseUser.uid)
        profileResult.fold(
            onSuccess = { firestoreUser ->
                Log.d(TAG, "Successfully loaded Firestore profile: ${firestoreUser != null}")
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

                Log.d(TAG, "Setting user as logged in. Profile complete: ${user.isProfileComplete}")
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    user = user,
                    needsProfileCompletion = !user.isProfileComplete
                )
            },
            onFailure = { error ->
                Log.e(TAG, "Failed to load Firestore profile: ${error.message}", error)
                // Fallback to Firebase Auth data if Firestore fails
                val user = User(
                    id = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: "",
                    username = null,
                    birthMonth = null,
                    birthYear = null,
                    isProfileComplete = false
                )

                Log.d(TAG, "Using fallback user data, setting profile completion needed")
                _uiState.value = _uiState.value.copy(
                    isLoggedIn = true,
                    user = user,
                    needsProfileCompletion = true
                )
            }
        )
    }

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

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting Google sign-in")
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                Log.d(TAG, "Launching Google credentials request")
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )

                val credential = result.credential
                Log.d(TAG, "Credential result received")
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                Log.d(TAG, "Google ID Token received: ${googleIdToken != null}")
                // Sign in to Firebase with Google ID token
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()

                val firebaseUser = authResult.user
                Log.d(TAG, "Firebase auth result user: ${firebaseUser?.uid}")
                if (firebaseUser != null) {
                    // Load user profile from Firestore to check if it's already complete
                    val profileResult = firebaseRepository.getUserProfile(firebaseUser.uid)
                    profileResult.fold(
                        onSuccess = { firestoreUser ->
                            Log.d(
                                TAG,
                                "Firestore user profile loaded for Google user: ${firestoreUser != null}"
                            )
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
                            Log.e(
                                TAG,
                                "Failed to load Firestore user profile after Google sign-in: ${it}"
                            )
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
                    Log.e(TAG, "Google sign-in failed: firebaseUser is null")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Google sign-in failed"
                    )
                }

            } catch (e: GetCredentialException) {
                Log.e(TAG, "Google sign-in cancelled or failed: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google sign-in cancelled or failed: ${e.message}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Google sign-in failed with exception: ${e.message}", e)
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
                Log.d(TAG, "Login initiated for: $email")
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                // Basic validation
                if (email.isBlank() || password.isBlank()) {
                    Log.d(TAG, "Login failed: empty fields")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please fill in all fields"
                    )
                    return@launch
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Log.d(TAG, "Login failed: invalid email format")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please enter a valid email address"
                    )
                    return@launch
                }

                // Firebase Email/Password Sign In
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user

                Log.d(TAG, "Firebase login result user: ${firebaseUser?.uid}")
                if (firebaseUser != null) {
                    // Load user profile from Firestore
                    val profileResult = firebaseRepository.getUserProfile(firebaseUser.uid)
                    profileResult.fold(
                        onSuccess = { firestoreUser ->
                            Log.d(
                                TAG,
                                "Firestore user profile loaded for login: ${firestoreUser != null}"
                            )
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
                            Log.e(
                                TAG,
                                "Failed to load Firestore user profile after login: ${it}"
                            )
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
                    Log.e(TAG, "Login failed: firebaseUser is null")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Login failed"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Login failed with exception: ${e.message}", e)
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
                Log.d(TAG, "Sign up initiated for: $email")
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

                // Basic validation
                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    Log.d(TAG, "Sign up failed: empty fields")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please fill in all fields"
                    )
                    return@launch
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Log.d(TAG, "Sign up failed: invalid email format")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Please enter a valid email address"
                    )
                    return@launch
                }

                if (password.length < 6) {
                    Log.d(TAG, "Sign up failed: password too short")
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

                Log.d(TAG, "Firebase sign up result user: ${firebaseUser?.uid}")
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
                    Log.e(TAG, "Sign up failed: firebaseUser is null")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Sign up failed"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Sign up failed with exception: ${e.message}", e)
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
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                firebaseAuth.signOut()
                // Reset to logged out state without triggering initialization wait
                _uiState.value = AuthUiState(
                    isLoading = false,
                    isInitializing = false,
                    isLoggedIn = false,
                    errorMessage = null,
                    user = null,
                    signupSuccess = false,
                    successMessage = null,
                    needsProfileCompletion = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Logout failed: ${e.message}"
                )
            }
        }
    }

    fun updateUserJourney(totalReadings: Int, currentStreak: Int, level: String) {
        viewModelScope.launch {
            try {
                val currentUser = _uiState.value.user
                if (currentUser != null) {
                    // Update user with new journey data (bestStreak removed)
                    val updatedUser = currentUser.copy(
                        totalReadings = totalReadings,
                        currentStreak = currentStreak,
                        level = level
                    )

                    // Save to Firestore database
                    val result = firebaseRepository.saveUserProfile(updatedUser)
                    result.fold(
                        onSuccess = {
                            _uiState.value = _uiState.value.copy(user = updatedUser)
                        },
                        onFailure = { error ->
                            _uiState.value = _uiState.value.copy(
                                errorMessage = "Failed to update journey: ${error.message}"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update journey: ${e.message}"
                )
            }
        }
    }

    fun incrementReading() {
        viewModelScope.launch {
            val currentUser = _uiState.value.user
            if (currentUser != null) {
                val newReadingCount = currentUser.totalReadings + 1
                val newLevel = calculateUserLevel(newReadingCount)

                updateUserJourney(
                    totalReadings = newReadingCount,
                    currentStreak = currentUser.currentStreak, // Streak will be handled by JourneyRepository
                    level = newLevel
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

    fun refreshUserData() {
        viewModelScope.launch {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val profileResult = firebaseRepository.getUserProfile(currentUser.uid)
                profileResult.fold(
                    onSuccess = { firestoreUser ->
                        if (firestoreUser != null) {
                            _uiState.value = _uiState.value.copy(user = firestoreUser)
                        }
                    },
                    onFailure = { /* Keep current state on error */ }
                )
            }
        }
    }

    // Test Firebase Auth persistence and token validity
    fun testAuthPersistence() {
        viewModelScope.launch {
            Log.d(TAG, "=== TESTING FIREBASE AUTH PERSISTENCE ===")

            val currentUser = firebaseAuth.currentUser
            Log.d(TAG, "Current user: ${currentUser?.uid}")
            Log.d(TAG, "User email: ${currentUser?.email}")
            Log.d(TAG, "User display name: ${currentUser?.displayName}")
            Log.d(TAG, "Is email verified: ${currentUser?.isEmailVerified}")

            if (currentUser != null) {
                try {
                    // Try to get ID token to test if auth is valid
                    val idTokenResult = currentUser.getIdToken(false).await()
                    Log.d(TAG, "ID Token retrieved successfully")
                    Log.d(TAG, "Token claims: ${idTokenResult.claims}")
                    Log.d(TAG, "Token expiration: ${idTokenResult.expirationTimestamp}")
                    Log.d(TAG, "Token issued at: ${idTokenResult.issuedAtTimestamp}")

                    // Try to get fresh token
                    val freshTokenResult = currentUser.getIdToken(true).await()
                    Log.d(TAG, "Fresh ID Token retrieved successfully")

                    // Test Firestore access
                    Log.d(TAG, "Testing Firestore access...")
                    val profileResult = firebaseRepository.getUserProfile(currentUser.uid)
                    profileResult.fold(
                        onSuccess = { user ->
                            Log.d(TAG, "Firestore access successful: ${user != null}")
                            Log.d(TAG, "User profile loaded: ${user?.name}")
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Firestore access failed: ${error.message}", error)
                        }
                    )

                } catch (e: Exception) {
                    Log.e(TAG, "Token retrieval failed: ${e.message}", e)
                    Log.d(TAG, "This indicates authentication state is corrupted")
                }
            } else {
                Log.d(TAG, "No current user - authentication not persisted")
                Log.d(TAG, "This means persistent login is NOT working")
            }

            Log.d(TAG, "=== END AUTH PERSISTENCE TEST ===")
        }
    }

    // Test force-close persistence specifically
    fun testForceClosePersistence() {
        viewModelScope.launch {
            Log.d(TAG, "=== TESTING FORCE-CLOSE PERSISTENCE ===")

            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                Log.d(TAG, "✅ PERSISTENT LOGIN SUCCESS: User found after app restart")
                Log.d(TAG, "User ID: ${currentUser.uid}")
                Log.d(TAG, "User Email: ${currentUser.email}")

                // Test if we can access user data
                try {
                    val token = currentUser.getIdToken(false).await()
                    Log.d(TAG, "✅ TOKEN VALID: Can retrieve authentication token")

                    val profileResult = firebaseRepository.getUserProfile(currentUser.uid)
                    profileResult.fold(
                        onSuccess = { user ->
                            Log.d(TAG, "✅ FIRESTORE ACCESS: Successfully loaded user profile")
                            Log.d(TAG, "Profile complete: ${user?.isProfileComplete}")
                        },
                        onFailure = { error ->
                            Log.e(TAG, "❌ FIRESTORE ACCESS FAILED: ${error.message}")
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "❌ TOKEN INVALID: ${e.message}")
                }
            } else {
                Log.e(TAG, "❌ PERSISTENT LOGIN FAILED: No user found after app restart")
                Log.d(TAG, "This means the keystore corruption issue persists")
            }

            Log.d(TAG, "=== END FORCE-CLOSE PERSISTENCE TEST ===")
        }
    }
}
