package com.example.tarot.data

import com.example.tarot.viewmodel.User

// Dummy authentication repository for testing purposes
class DummyAuthRepository {

    // Predefined test users for easy testing
    private val testUsers = listOf(
        User(
            id = "test_001",
            name = "Test User",
            email = "test@test.com"
        )
    )

    // Valid test credentials (email -> password)
    private val testCredentials = mapOf(
        "test@test.com" to "test"
    )

    // Simulate login with dummy data
    suspend fun login(email: String, password: String): LoginResult {
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

    // Simulate sign up with dummy data
    suspend fun signUp(name: String, email: String, password: String): LoginResult {
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

    // Simulate forgot password
    suspend fun forgotPassword(email: String): Boolean {
        return true // Always succeeds in test mode
    }

    // Get a hint about test credentials for developers
    fun getTestCredentialsHint(): String {
        return "Try test@test.com / test"
    }

    // Get all available test accounts for debugging
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

// Result class for login operations
sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

// Test account data class for debugging purposes
data class TestAccount(
    val email: String,
    val password: String,
    val name: String
)
