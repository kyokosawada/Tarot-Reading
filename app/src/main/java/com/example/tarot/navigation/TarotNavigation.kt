package com.example.tarot.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.tarot.ui.screens.auth.ForgotPasswordScreen
import com.example.tarot.ui.screens.auth.LoginScreen
import com.example.tarot.ui.screens.auth.SignUpScreen
import com.example.tarot.ui.screens.home.HomeScreen
import com.example.tarot.ui.screens.home.ProfileScreen
import com.example.tarot.ui.screens.reading.AskQuestionScreen
import com.example.tarot.ui.screens.reading.DailyReadingScreen
import com.example.tarot.viewmodel.AuthViewModel

sealed class Screen {
    object Login : Screen()
    object SignUp : Screen()
    object ForgotPassword : Screen()
    object Home : Screen()
    object Profile : Screen()
    object DailyReading : Screen()
    object AskQuestion : Screen()
    // object History : Screen() // Reserved for future implementation
}

@Composable
fun TarotNavigation(
    authViewModel: AuthViewModel
) {
    val authUiState by authViewModel.uiState.collectAsState()
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Handle login success - navigate to home
    if (authUiState.isLoggedIn && currentScreen == Screen.Login) {
        currentScreen = Screen.Home
    }

    // Handle signup success - navigate to login
    if (authUiState.signupSuccess && currentScreen == Screen.SignUp) {
        currentScreen = Screen.Login
    }

    // Handle logout
    if (!authUiState.isLoggedIn && currentScreen !in listOf(
            Screen.Login,
            Screen.SignUp,
            Screen.ForgotPassword
        )
    ) {
        currentScreen = Screen.Login
    }

    // Handle Android back button properly
    when (currentScreen) {
        Screen.SignUp, Screen.ForgotPassword -> {
            BackHandler {
                currentScreen = Screen.Login
            }
        }
        Screen.Profile, Screen.DailyReading, Screen.AskQuestion -> {
            BackHandler {
                currentScreen = Screen.Home
            }
        }
        Screen.Home -> {
            BackHandler {
                // Allow back press to minimize app instead of closing
                // This is the root screen for logged-in users
            }
        }

        Screen.Login -> {
            // Allow back press to minimize app on login screen
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        when (currentScreen) {
            is Screen.Login -> {
                LoginScreen(
                    onLoginClick = { email, password ->
                        authViewModel.clearMessages()
                        authViewModel.login(email, password)
                    },
                    onGoogleSignInClick = {
                        authViewModel.clearMessages()
                        authViewModel.signInWithGoogle(context)
                    },
                    onSignUpClick = {
                        authViewModel.clearMessages()
                        currentScreen = Screen.SignUp
                    },
                    onForgotPasswordClick = {
                        authViewModel.clearMessages()
                        currentScreen = Screen.ForgotPassword
                    },
                    authUiState = authUiState,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is Screen.SignUp -> {
                SignUpScreen(
                    onSignUpClick = { name, email, password ->
                        authViewModel.signUp(name, email, password)
                    },
                    onSignInClick = {
                        authViewModel.clearMessages()
                        currentScreen = Screen.Login
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is Screen.ForgotPassword -> {
                ForgotPasswordScreen(
                    onBackClick = {
                        authViewModel.clearMessages()
                        currentScreen = Screen.Login
                    },
                    onResetPasswordClick = { email ->
                        // Handle password reset - for now just go back to login
                        authViewModel.clearMessages()
                        currentScreen = Screen.Login
                    },
                    onSuccessNavigation = {
                        authViewModel.clearMessages()
                        currentScreen = Screen.Login
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is Screen.Home -> {
                HomeScreen(
                    onNavigateToReading = { readingType ->
                        currentScreen = when (readingType) {
                            "daily" -> Screen.DailyReading
                            "question" -> Screen.AskQuestion
                            else -> Screen.DailyReading
                        }
                    },
                    onNavigateToProfile = {
                        authViewModel.clearMessages()
                        currentScreen = Screen.Profile
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is Screen.Profile -> {
                ProfileScreen(
                    onBackClick = {
                        authViewModel.clearMessages()
                        currentScreen = Screen.Home
                    },
                    onEditProfileClick = {
                        // Handle edit profile
                    },
                    onLogoutClick = {
                        authViewModel.clearMessages()
                        authViewModel.logout()
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is Screen.DailyReading -> {
                DailyReadingScreen(
                    onBackClick = {
                        authViewModel.clearMessages()
                        currentScreen = Screen.Home
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is Screen.AskQuestion -> {
                AskQuestionScreen(
                    onBackClick = {
                        authViewModel.clearMessages()
                        currentScreen = Screen.Home
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}
