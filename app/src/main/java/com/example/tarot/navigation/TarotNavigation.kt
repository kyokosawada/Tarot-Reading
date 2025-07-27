package com.example.tarot.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.tarot.ui.screens.SplashScreen
import com.example.tarot.ui.screens.auth.ForgotPasswordScreen
import com.example.tarot.ui.screens.auth.LoginScreen
import com.example.tarot.ui.screens.auth.ProfileCompletionScreen
import com.example.tarot.ui.screens.auth.SignUpScreen
import com.example.tarot.ui.screens.history.ReadingHistoryScreen
import com.example.tarot.ui.screens.home.HomeScreen
import com.example.tarot.ui.screens.home.ProfileScreen
import com.example.tarot.ui.screens.reading.AskQuestionScreen
import com.example.tarot.ui.screens.reading.DailyReadingScreen
import com.example.tarot.ui.screens.reading.PalmReadingScreen
import com.example.tarot.ui.screens.settings.SettingsScreen
import com.example.tarot.viewmodel.AuthViewModel

// Navigation routes
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "signup"
    const val FORGOT_PASSWORD = "forgot_password"
    const val PROFILE_COMPLETION = "profile_completion"
    const val HOME = "home"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val DAILY_READING = "daily_reading"
    const val ASK_QUESTION = "ask_question"
    const val PALM_READING = "palm_reading"
    const val READING_HISTORY = "reading_history" // Add reading history route
}

// Animation constants
private const val ANIMATION_DURATION = 400
private const val FADE_DURATION = 200
private const val TAG = "TarotNavigation"

@Composable
fun TarotNavigation(
    authViewModel: AuthViewModel
) {
    val authUiState by authViewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val context = LocalContext.current


    // Handle authentication state changes after initialization
    LaunchedEffect(
        authUiState.isInitializing,
        authUiState.isLoggedIn,
        authUiState.needsProfileCompletion,
        authUiState.isSigningUp
    ) {
        // Wait for initialization to complete and signup to finish
        if (!authUiState.isInitializing && !authUiState.isSigningUp) {
            try {
                when {
                    authUiState.signupSuccess -> {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.SIGN_UP) { inclusive = true }
                        }
                    }
                    authUiState.isLoggedIn && authUiState.needsProfileCompletion -> {
                        navController.navigate(Routes.PROFILE_COMPLETION) {
                            popUpTo(0) { inclusive = true } // Clear entire back stack
                        }
                    }
                    authUiState.isLoggedIn && !authUiState.needsProfileCompletion -> {
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute != Routes.HOME) {
                            navController.navigate(Routes.HOME) {
                                popUpTo(0) { inclusive = true } // Clear entire back stack
                            }
                        }
                    }
                    !authUiState.isLoggedIn -> {
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        // Navigate to login from any screen when logged out, including splash
                        if (currentRoute != Routes.LOGIN && currentRoute != Routes.SIGN_UP && currentRoute != Routes.FORGOT_PASSWORD) {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(0) { inclusive = true } // Clear entire back stack
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Log navigation errors but don't crash the app
                android.util.Log.e(TAG, "Navigation error", e)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash screen
            composable(
                route = Routes.SPLASH,
                enterTransition = { fadeIn(animationSpec = tween(FADE_DURATION)) },
                exitTransition = { fadeOut(animationSpec = tween(FADE_DURATION)) }
            ) {
                SplashScreen()
            }

            // Auth screens with horizontal slide animations
            composable(
                route = Routes.LOGIN,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.SIGN_UP, Routes.FORGOT_PASSWORD -> slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(ANIMATION_DURATION)
                        )

                        else -> fadeIn(animationSpec = tween(FADE_DURATION))
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.SIGN_UP, Routes.FORGOT_PASSWORD -> slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(ANIMATION_DURATION)
                        )

                        else -> fadeOut(animationSpec = tween(FADE_DURATION))
                    }
                }
            ) {
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
                        navController.navigate(Routes.SIGN_UP)
                    },
                    onForgotPasswordClick = {
                        authViewModel.clearMessages()
                        navController.navigate(Routes.FORGOT_PASSWORD)
                    },
                    authUiState = authUiState
                )
            }

            composable(
                route = Routes.SIGN_UP,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                }
            ) {
                SignUpScreen(
                    onSignUpClick = { name, email, password, month, year ->
                        authViewModel.signUp(name, email, password, month, year)
                    },
                    onSignInClick = {
                        authViewModel.clearMessages()
                        navController.popBackStack()
                    }
                )

                // Handle back press
                BackHandler {
                    navController.popBackStack()
                }
            }

            composable(
                route = Routes.FORGOT_PASSWORD,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                }
            ) {
                ForgotPasswordScreen(
                    onBackClick = {
                        authViewModel.clearMessages()
                        navController.popBackStack()
                    },
                    onResetPasswordClick = { email ->
                        authViewModel.clearMessages()
                        navController.popBackStack()
                    },
                    onSuccessNavigation = {
                        authViewModel.clearMessages()
                        navController.popBackStack()
                    }
                )

                // Handle back press
                BackHandler {
                    navController.popBackStack()
                }
            }

            composable(
                route = Routes.PROFILE_COMPLETION,
                enterTransition = {
                    fadeIn(animationSpec = tween(FADE_DURATION))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(FADE_DURATION))
                }
            ) {
                ProfileCompletionScreen(
                    onCompleteProfile = { username, month, year ->
                        authViewModel.completeProfile(username, month, year)
                    },
                    authUiState = authUiState
                )

                // Prevent back navigation from profile completion
                BackHandler {
                    // Do nothing - user must complete profile
                }
            }

            // Main app screens with vertical slide animations
            composable(
                route = Routes.HOME,
                enterTransition = {
                    when (initialState.destination.route) {
                        Routes.PROFILE, Routes.SETTINGS -> slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Down,
                            animationSpec = tween(ANIMATION_DURATION)
                        )

                        Routes.DAILY_READING, Routes.ASK_QUESTION -> slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = tween(ANIMATION_DURATION)
                        )

                        else -> fadeIn(animationSpec = tween(FADE_DURATION))
                    }
                },
                exitTransition = {
                    when (targetState.destination.route) {
                        Routes.PROFILE, Routes.SETTINGS -> slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Up,
                            animationSpec = tween(ANIMATION_DURATION)
                        )

                        Routes.DAILY_READING, Routes.ASK_QUESTION -> slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = tween(ANIMATION_DURATION)
                        )

                        else -> fadeOut(animationSpec = tween(FADE_DURATION))
                    }
                }
            ) {
                HomeScreen(
                    authViewModel = authViewModel,
                    onNavigateToReading = { readingType ->
                        val route = when (readingType) {
                            "daily" -> Routes.DAILY_READING
                            "question" -> Routes.ASK_QUESTION
                            "palm" -> Routes.PALM_READING
                            else -> Routes.DAILY_READING
                        }
                        navController.navigate(route)
                    },
                    onNavigateToProfile = {
                        authViewModel.clearMessages()
                        navController.navigate(Routes.PROFILE)
                    }
                )
            }

            composable(
                route = Routes.PROFILE,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                }
            ) {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onBackClick = {
                        authViewModel.clearMessages()
                        navController.popBackStack()
                    },
                    onEditProfileClick = {
                        // Handle edit profile
                    },
                    onSettingsClick = {
                        navController.navigate(Routes.SETTINGS)
                    },
                    onReadingHistoryClick = {
                        navController.navigate(Routes.READING_HISTORY)
                    },
                    onLogoutClick = {
                        authViewModel.clearMessages()
                        authViewModel.logout()
                    }
                )

                // Handle back press
                BackHandler {
                    navController.popBackStack()
                }
            }

            composable(
                route = Routes.SETTINGS,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                }
            ) {
                SettingsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )

                // Handle back press
                BackHandler {
                    navController.popBackStack()
                }
            }

            composable(
                route = Routes.DAILY_READING,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                }
            ) {
                DailyReadingScreen(
                    onBackClick = {
                        authViewModel.clearMessages()
                        navController.popBackStack()
                    }
                )

                // Handle back press
                BackHandler {
                    navController.popBackStack()
                }
            }

            composable(
                route = Routes.ASK_QUESTION,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                }
            ) {
                AskQuestionScreen(
                    onBackClick = {
                        authViewModel.clearMessages()
                        navController.popBackStack()
                    }
                )

                // Handle back press
                BackHandler {
                    navController.popBackStack()
                }
            }

            composable(
                route = Routes.PALM_READING,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                }
            ) {
                PalmReadingScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )

                // Handle back press
                BackHandler {
                    navController.popBackStack()
                }
            }

            // Reading History screen
            composable(
                route = Routes.READING_HISTORY,
                enterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                },
                exitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(ANIMATION_DURATION)
                    )
                }
            ) {
                ReadingHistoryScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )

                // Handle back press
                BackHandler {
                    navController.popBackStack()
                }
            }
        }
    }
}
