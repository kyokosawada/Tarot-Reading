package com.example.tarot.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarot.ui.components.MysticaLogoCompact
import com.example.tarot.ui.theme.ErrorRed
import com.example.tarot.ui.theme.MysticDarkBlue
import com.example.tarot.ui.theme.MysticGold
import com.example.tarot.ui.theme.MysticSilver
import com.example.tarot.ui.theme.MysticaGradientEnd
import com.example.tarot.ui.theme.MysticaGradientMid
import com.example.tarot.ui.theme.MysticaGradientStart
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.ui.theme.TextPrimary
import com.example.tarot.ui.theme.TextSecondary
import com.example.tarot.viewmodel.AuthUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onGoogleSignInClick: () -> Unit = {},
    authUiState: AuthUiState? = null,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isEmailError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MysticaGradientStart,
                        MysticaGradientMid,
                        MysticaGradientEnd
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section - Logo and Welcome
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.35f),
                verticalArrangement = Arrangement.Center
            ) {
                MysticaLogoCompact(
                    logoSize = 100,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Welcome back to the mystical realm, dear seeker.",
                    fontSize = 14.sp,
                    color = TextSecondary.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Middle Section - Login Form
            Column(
                modifier = Modifier.weight(0.5f),
                verticalArrangement = Arrangement.Center
            ) {
                // Error Message
                authUiState?.errorMessage?.let { errorMessage ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = ErrorRed.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = ErrorRed,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        isEmailError = false
                    },
                    label = { Text("Email", color = TextSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon",
                            tint = MysticGold
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    isError = isEmailError,
                    enabled = authUiState?.isLoading != true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MysticGold,
                        unfocusedBorderColor = MysticSilver.copy(alpha = 0.6f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = MysticGold
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        isPasswordError = false
                    },
                    label = { Text("Password", color = TextSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Password Icon",
                            tint = MysticGold
                        )
                    },
                    trailingIcon = {
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(
                                text = if (passwordVisible) "Hide" else "Show",
                                color = MysticGold,
                                fontSize = 11.sp
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    isError = isPasswordError,
                    enabled = authUiState?.isLoading != true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MysticGold,
                        unfocusedBorderColor = MysticSilver.copy(alpha = 0.6f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = MysticGold
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                // Login Button
                Button(
                    onClick = {
                        when {
                            email.isBlank() -> isEmailError = true
                            password.isBlank() -> isPasswordError = true
                            else -> onLoginClick(email, password)
                        }
                    },
                    enabled = authUiState?.isLoading != true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MysticGold,
                        contentColor = MysticDarkBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (authUiState?.isLoading == true) {
                        Text(
                            text = "SIGNING IN...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    } else {
                        Text(
                            text = "LOG IN",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Forgot Password
                TextButton(
                    onClick = onForgotPasswordClick,
                    enabled = authUiState?.isLoading != true,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "FORGOT PASSWORD",
                        color = MysticGold.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Bottom Section - Sign Up Link
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(0.15f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    TextButton(
                        onClick = onSignUpClick,
                        enabled = authUiState?.isLoading != true
                    ) {
                        Text(
                            text = "Sign Up",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Optional Google Sign-in as subtle link
                if (onGoogleSignInClick != {}) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onGoogleSignInClick,
                        enabled = authUiState?.isLoading != true
                    ) {
                        Text(
                            text = "Continue with Google",
                            color = MysticSilver.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    TarotTheme {
        LoginScreen()
    }
}

@Preview(showBackground = true, name = "Login Screen - Compact", widthDp = 320, heightDp = 640)
@Composable
fun LoginScreenCompactPreview() {
    TarotTheme {
        LoginScreen()
    }
}