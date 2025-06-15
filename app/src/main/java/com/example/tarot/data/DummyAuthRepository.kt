package com.example.tarot.data

import com.example.tarot.viewmodel.User
import kotlinx.coroutines.delay

/**
 * Dummy authentication repository for testing purposes
 * Contains predefined test users for easy mobile testing
 */
class DummyAuthRepository {

    // Predefined test users for easy testing
    private val testUsers = listOf(
        User(
            id = "test_001",
            name = "Mystic Luna",
            email = "luna@test.com"
        ),
        User(
            id = "test_002",
            name = "Sage Oracle",
            email = "sage@test.com"
        ),
        User(
            id = "test_003",
            name = "Crystal Reader",
            email = "crystal@test.com"
        ),
        User(
            id = "test_004",
            name = "Tarot Master",
            email = "master@test.com"
        ),
        User(
            id = "test_005",
            name = "Spiritual Guide",
            email = "guide@test.com"
        )
    )

    // Valid test credentials (email -> password)
    private val testCredentials = mapOf(
        "luna@test.com" to "password123",
        "sage@test.com" to "mystic456",
        "crystal@test.com" to "tarot789",
        "master@test.com" to "cards101",
        "guide@test.com" to "spirit202",
        // Quick login options
        "test@test.com" to "test",
        "admin@test.com" to "admin",
        "demo@test.com" to "demo"
    )

    /**
     * Simulate login with dummy data
     * @param email User email
     * @param password User password
     * @return LoginResult containing success status and user data
     */
    suspend fun login(email: String, password: String): LoginResult {
        // Simulate network delay
        delay(800)

        // Check if credentials are valid
        val expectedPassword = testCredentials[email.lowercase()]

        return if (expectedPassword != null && expectedPassword == password) {
            // Find user or create a generic one
            val user = testUsers.find { it.email.equals(email, ignoreCase = true) }
                ?: User(
                    id = "generic_${System.currentTimeMillis()}",
                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    email = email
                )

            LoginResult.Success(user)
        } else {
            LoginResult.Error("Invalid email or password")
        }
    }

    /**
     * Simulate sign up with dummy data
     * Always succeeds for testing purposes
     */
    suspend fun signUp(name: String, email: String, password: String): LoginResult {
        // Simulate network delay
        delay(1200)

        // Check if email already exists in test data
        if (testCredentials.containsKey(email.lowercase())) {
            return LoginResult.Error("Email already exists. Try: ${getTestCredentialsHint()}")
        }

        // Create new user
        val user = User(
            id = "new_${System.currentTimeMillis()}",
            name = name,
            email = email
        )

        return LoginResult.Success(user)
    }

    /**
     * Simulate forgot password - always succeeds for testing
     */
    suspend fun forgotPassword(email: String): Boolean {
        delay(600)
        return true // Always succeeds in test mode
    }

    /**
     * Get a hint about test credentials for developers
     */
    fun getTestCredentialsHint(): String {
        return "Try luna@test.com / password123 or test@test.com / test"
    }

    /**
     * Get all available test accounts for debugging
     */
    fun getAllTestAccounts(): List<TestAccount> {
        return testCredentials.map { (email, password) ->
            val user = testUsers.find { it.email.equals(email, ignoreCase = true) }
            TestAccount(
                email = email,
                password = password,
                name = user?.name ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
            )
        }
    }
}

/**
 * Result class for login operations
 */
sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

/**
 * Test account data class for debugging purposes
 */
data class TestAccount(
    val email: String,
    val password: String,
    val name: String
)