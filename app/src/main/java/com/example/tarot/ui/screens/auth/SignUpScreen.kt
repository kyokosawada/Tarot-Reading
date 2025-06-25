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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.tarot.ui.theme.MysticDarkBlue
import com.example.tarot.ui.theme.MysticGold
import com.example.tarot.ui.theme.MysticNavy
import com.example.tarot.ui.theme.MysticSilver
import com.example.tarot.ui.theme.MysticaGradientEnd
import com.example.tarot.ui.theme.MysticaGradientMid
import com.example.tarot.ui.theme.MysticaGradientStart
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.ui.theme.TextPrimary
import com.example.tarot.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpClick: (String, String, String) -> Unit = { _, _, _ -> },
    onSignInClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var acceptTerms by remember { mutableStateOf(false) }
    
    var isNameError by remember { mutableStateOf(false) }
    var isEmailError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }
    var isConfirmPasswordError by remember { mutableStateOf(false) }
    var passwordMismatch by remember { mutableStateOf(false) }

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
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(modifier = Modifier.height(32.dp))

        // App Logo/Title
            // App Logo with MYSTICA branding
            MysticaLogoCompact(
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Sign Up Form Card
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
                    Text(
                        text = "Create Account",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MysticGold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Join the mystical journey",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

        // Full Name Field
        OutlinedTextField(
            value = fullName,
            onValueChange = { 
                fullName = it
                isNameError = false
            },
            label = { Text("Full Name", color = TextSecondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Name Icon",
                    tint = MysticGold
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            isError = isNameError,
            supportingText = if (isNameError) {
                { Text("Please enter your full name", color = MaterialTheme.colorScheme.error) }
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
            supportingText = if (isEmailError) {
                { Text("Please enter a valid email", color = MaterialTheme.colorScheme.error) }
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
                passwordMismatch = false
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
                imeAction = ImeAction.Next
            ),
            isError = isPasswordError,
            supportingText = if (isPasswordError) {
                {
                    Text(
                        "Password must be at least 6 characters",
                        color = MaterialTheme.colorScheme.error
                    )
                }
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

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it
                isConfirmPasswordError = false
                passwordMismatch = false
            },
            label = { Text("Confirm Password", color = TextSecondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Confirm Password Icon",
                    tint = MysticGold
                )
            },
            trailingIcon = {
                TextButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Text(
                        text = if (confirmPasswordVisible) "Hide" else "Show",
                        color = MysticGold,
                        fontSize = 12.sp
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            isError = isConfirmPasswordError || passwordMismatch,
            supportingText = if (isConfirmPasswordError) {
                { Text("Please confirm your password", color = MaterialTheme.colorScheme.error) }
            } else if (passwordMismatch) {
                { Text("Passwords do not match", color = MaterialTheme.colorScheme.error) }
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

        // Terms and Conditions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = acceptTerms,
                onCheckedChange = { acceptTerms = it },
                colors = CheckboxDefaults.colors(
                    checkedColor = MysticGold,
                    uncheckedColor = MysticSilver,
                    checkmarkColor = MysticDarkBlue
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "I agree to the Terms of Service and Privacy Policy",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        // Sign Up Button
        Button(
            onClick = {
                val isValid = when {
                    fullName.isBlank() -> {
                        isNameError = true
                        false
                    }
                    email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                        isEmailError = true
                        false
                    }
                    password.length < 6 -> {
                        isPasswordError = true
                        false
                    }
                    confirmPassword.isBlank() -> {
                        isConfirmPasswordError = true
                        false
                    }
                    password != confirmPassword -> {
                        passwordMismatch = true
                        false
                    }
                    else -> true
                }
                
                if (isValid) {
                    onSignUpClick(fullName, email, password)
                }
            },
            enabled = acceptTerms,
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
                text = "Create Account",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign In Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? ",
                fontSize = 14.sp,
                color = TextSecondary
            )
            TextButton(onClick = onSignInClick) {
                Text(
                    text = "Sign In",
                    color = MysticGold,
                    fontWeight = FontWeight.Medium
                )
            }
        }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    TarotTheme {
        SignUpScreen()
    }
}

@Preview(showBackground = true, name = "SignUp Screen - Dark Theme")
@Composable
fun SignUpScreenDarkPreview() {
    TarotTheme(darkTheme = true) {
        SignUpScreen()
    }
}

@Preview(showBackground = true, name = "SignUp Screen - Compact", widthDp = 320, heightDp = 720)
@Composable
fun SignUpScreenCompactPreview() {
    TarotTheme {
        SignUpScreen()
    }
}
