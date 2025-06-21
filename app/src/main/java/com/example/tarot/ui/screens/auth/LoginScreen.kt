package com.example.tarot.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarot.ui.theme.BackgroundEnd
import com.example.tarot.ui.theme.BackgroundStart
import com.example.tarot.ui.theme.ErrorRed
import com.example.tarot.ui.theme.MysticCosmic
import com.example.tarot.ui.theme.MysticDarkBlue
import com.example.tarot.ui.theme.MysticGold
import com.example.tarot.ui.theme.MysticNavy
import com.example.tarot.ui.theme.MysticSilver
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.ui.theme.TextAccent
import com.example.tarot.ui.theme.TextPrimary
import com.example.tarot.ui.theme.TextSecondary
import com.example.tarot.viewmodel.AuthUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
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
                        BackgroundStart,
                        BackgroundEnd
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // MYSTICA Branding
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔮",
                    fontSize = 72.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = "MYSTICA",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextAccent,
                    letterSpacing = 6.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Tarot & Divination",
                    fontSize = 16.sp,
                    color = TextSecondary,
                    letterSpacing = 3.sp,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
            }

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
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Success Message
            authUiState?.successMessage?.let { successMessage ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MysticGold.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = successMessage,
                        color = MysticGold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Login Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MysticNavy.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
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
                        supportingText = if (isEmailError) {
                            { Text("Please enter a valid email", color = ErrorRed) }
                        } else null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MysticGold,
                            unfocusedBorderColor = MysticSilver,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = MysticGold
                        ),
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
                                    fontSize = 12.sp
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
                        supportingText = if (isPasswordError) {
                            { Text("Password is required", color = ErrorRed) }
                        } else null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MysticGold,
                            unfocusedBorderColor = MysticSilver,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = MysticGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )

                    // Forgot Password
                    TextButton(
                        onClick = onForgotPasswordClick,
                        enabled = authUiState?.isLoading != true,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            "Forgot Password?",
                            color = MysticSilver,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

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
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MysticGold,
                            contentColor = MysticDarkBlue
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        if (authUiState?.isLoading == true) {
                            Text(
                                text = "Signing In...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "Sign In",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MysticSilver.copy(alpha = 0.3f)
                        )
                        Text(
                            text = "OR",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = TextSecondary,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MysticSilver.copy(alpha = 0.3f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign Up Button
                    OutlinedButton(
                        onClick = onSignUpClick,
                        enabled = authUiState?.isLoading != true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                colors = listOf(MysticGold, MysticSilver)
                            )
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(
                            text = "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Test Accounts Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MysticCosmic.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Test Account: test@test.com / test",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
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
