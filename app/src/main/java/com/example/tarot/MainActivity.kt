package com.example.tarot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tarot.ui.screens.auth.LoginScreen
import com.example.tarot.ui.screens.auth.SignUpScreen
import com.example.tarot.ui.screens.auth.TestAccountsScreen
import com.example.tarot.ui.screens.home.HomeScreen
import com.example.tarot.ui.screens.home.ProfileScreen
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TarotTheme {
                TarotApp()
            }
        }
    }
}

@Composable
fun TarotApp() {
    val authViewModel: AuthViewModel = viewModel()
    val authUiState by authViewModel.uiState.collectAsState()

    var currentScreen by remember { mutableStateOf(Screen.Login) }

    // Navigate based on authentication state
    LaunchedEffect(authUiState.isLoggedIn) {
        if (authUiState.isLoggedIn) {
            currentScreen = Screen.Home
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (currentScreen) {
            Screen.Login -> {
                LoginScreen(
                    onLoginClick = { email, password ->
                        authViewModel.login(email, password)
                    },
                    onSignUpClick = {
                        currentScreen = Screen.SignUp
                    },
                    onForgotPasswordClick = {
                        // Handle forgot password
                    },
                    onTestAccountsClick = {
                        currentScreen = Screen.TestAccounts
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            Screen.SignUp -> {
                SignUpScreen(
                    onSignUpClick = { name, email, password ->
                        authViewModel.signUp(name, email, password)
                    },
                    onSignInClick = {
                        currentScreen = Screen.Login
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            Screen.Home -> {
                HomeScreen(
                    onNavigateToReading = { readingType ->
                        // Handle navigation to reading screen
                        // For now, just show a placeholder
                    },
                    onNavigateToHistory = {
                        // Handle navigation to history screen
                    },
                    onNavigateToProfile = {
                        currentScreen = Screen.Profile
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            Screen.Profile -> {
                ProfileScreen(
                    onBackClick = {
                        currentScreen = Screen.Home
                    },
                    onEditProfileClick = {
                        // Handle edit profile
                    },
                    onLogoutClick = {
                        authViewModel.logout()
                        currentScreen = Screen.Login
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            Screen.TestAccounts -> {
                TestAccountsScreen(
                    onBackClick = {
                        currentScreen = Screen.Login
                    },
                    onAccountClick = { email, password ->
                        authViewModel.login(email, password)
                        currentScreen = Screen.Login
                    }
                )
            }
        }
    }
}

enum class Screen {
    Login,
    SignUp,
    Home,
    Profile,
    TestAccounts
}