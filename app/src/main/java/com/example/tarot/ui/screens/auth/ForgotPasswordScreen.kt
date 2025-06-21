package com.example.tarot.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarot.ui.theme.BackgroundEnd
import com.example.tarot.ui.theme.BackgroundStart
import com.example.tarot.ui.theme.ErrorRed
import com.example.tarot.ui.theme.MysticDarkBlue
import com.example.tarot.ui.theme.MysticGold
import com.example.tarot.ui.theme.MysticNavy
import com.example.tarot.ui.theme.MysticSilver
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.ui.theme.TextAccent
import com.example.tarot.ui.theme.TextPrimary
import com.example.tarot.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit = {},
    onResetPasswordClick: (String) -> Unit = {},
    onSuccessNavigation: () -> Unit = {},
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var isEmailError by remember { mutableStateOf(false) }
    var showSuccessState by remember { mutableStateOf(false) }

    // Show success state if we have a success message
    if (successMessage != null && !showSuccessState) {
        showSuccessState = true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reset Password",
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MysticDarkBlue,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = MysticDarkBlue
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

                if (showSuccessState) {
                    // Success State
                    SuccessContent(
                        email = email,
                        onBackToLogin = onSuccessNavigation
                    )
                } else {
                    // Reset Password Form
                    ResetPasswordContent(
                        email = email,
                        onEmailChange = {
                            email = it
                            isEmailError = false
                        },
                        onResetClick = {
                            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(
                                    email
                                ).matches()
                            ) {
                                isEmailError = true
                            } else {
                                onResetPasswordClick(email)
                            }
                        },
                        isEmailError = isEmailError,
                        isLoading = isLoading,
                        errorMessage = errorMessage
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ResetPasswordContent(
    email: String,
    onEmailChange: (String) -> Unit,
    onResetClick: () -> Unit,
    isEmailError: Boolean,
    isLoading: Boolean,
    errorMessage: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // MYSTICA Branding
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
            text = "Password Recovery",
            fontSize = 16.sp,
            color = TextSecondary,
            letterSpacing = 3.sp,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Instructions
        Text(
            text = "Enter your email address and we'll send you a link to reset your password.",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Error Message
        errorMessage?.let { error ->
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
                    text = error,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Reset Form Card
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
                    onValueChange = onEmailChange,
                    label = { Text("Email Address", color = TextSecondary) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email Icon",
                            tint = MysticGold
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    isError = isEmailError,
                    enabled = !isLoading,
                    supportingText = if (isEmailError) {
                        { Text("Please enter a valid email address", color = ErrorRed) }
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
                        .padding(bottom = 24.dp)
                )

                // Reset Button
                Button(
                    onClick = onResetClick,
                    enabled = !isLoading && email.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MysticGold,
                        contentColor = MysticDarkBlue,
                        disabledContainerColor = MysticSilver.copy(alpha = 0.3f),
                        disabledContentColor = TextSecondary
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    if (isLoading) {
                        Text(
                            text = "Sending...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = "Send Reset Link",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuccessContent(
    email: String,
    onBackToLogin: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success Icon
        Text(
            text = "✅",
            fontSize = 72.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Check Your Email",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextAccent,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "We've sent a password reset link to:",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = email,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MysticGold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Check your email and click the link to reset your password. If you don't see it, check your spam folder.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Back to Login Button
        Button(
            onClick = onBackToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MysticGold,
                contentColor = MysticDarkBlue
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = "Back to Login",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resend Link
        TextButton(
            onClick = { /* Implement resend functionality */ }
        ) {
            Text(
                text = "Didn't receive the email? Resend",
                color = MysticSilver,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    TarotTheme {
        ForgotPasswordScreen()
    }
}

@Preview(showBackground = true, name = "Success State")
@Composable
fun ForgotPasswordSuccessPreview() {
    TarotTheme {
        ForgotPasswordScreen(
            successMessage = "Email sent successfully"
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
fun ForgotPasswordErrorPreview() {
    TarotTheme {
        ForgotPasswordScreen(
            errorMessage = "Email address not found"
        )
    }
}