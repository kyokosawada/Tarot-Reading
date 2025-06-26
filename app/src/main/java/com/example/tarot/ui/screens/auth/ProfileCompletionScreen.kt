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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.example.tarot.ui.components.MysticaLogoCompact
import com.example.tarot.ui.theme.ErrorRed
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
import com.example.tarot.viewmodel.AuthUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileCompletionScreen(
    onCompleteProfile: (Int, Int) -> Unit = { _, _ -> },
    authUiState: AuthUiState? = null,
    modifier: Modifier = Modifier
) {
    var selectedMonth by remember { mutableStateOf("") }
    var selectedYear by remember { mutableStateOf("") }
    var monthExpanded by remember { mutableStateOf(false) }

    val months = listOf(
        "January" to 1, "February" to 2, "March" to 3, "April" to 4,
        "May" to 5, "June" to 6, "July" to 7, "August" to 8,
        "September" to 9, "October" to 10, "November" to 11, "December" to 12
    )

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

            // App Logo
            MysticaLogoCompact(
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Profile Completion Form Card
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
                        text = "Complete Your Profile",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MysticGold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "We need your birth date to enhance your tarot reading experience with personalized cosmic insights",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

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

                    // Birth Month Dropdown
                    ExposedDropdownMenuBox(
                        expanded = monthExpanded,
                        onExpandedChange = { monthExpanded = !monthExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedMonth,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Birth Month", color = TextSecondary) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Month Icon",
                                    tint = MysticGold
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MysticGold,
                                unfocusedBorderColor = MysticSilver,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = MysticGold
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = monthExpanded,
                            onDismissRequest = { monthExpanded = false }
                        ) {
                            months.forEach { (monthName, monthNumber) ->
                                DropdownMenuItem(
                                    text = { Text(monthName) },
                                    onClick = {
                                        selectedMonth = monthName
                                        monthExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Birth Year Field
                    OutlinedTextField(
                        value = selectedYear,
                        onValueChange = { input ->
                            // Only allow numeric input and limit to 4 digits
                            if (input.all { it.isDigit() } && input.length <= 4) {
                                selectedYear = input
                            }
                        },
                        label = { Text("Birth Year", color = TextSecondary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Year Icon",
                                tint = MysticGold
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        placeholder = {
                            Text(
                                "e.g., 1990",
                                color = TextSecondary.copy(alpha = 0.6f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MysticGold,
                            unfocusedBorderColor = MysticSilver,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = MysticGold
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    )

                    // Complete Profile Button
                    Button(
                        onClick = {
                            val monthNumber = months.find { it.first == selectedMonth }?.second
                            val year = selectedYear.toIntOrNull()

                            if (monthNumber != null && year != null) {
                                onCompleteProfile(monthNumber, year)
                            }
                        },
                        enabled = selectedMonth.isNotBlank() &&
                                selectedYear.isNotBlank() &&
                                authUiState?.isLoading != true,
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = MysticDarkBlue,
                                    modifier = Modifier
                                        .width(20.dp)
                                        .height(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Completing...",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = "Complete Profile",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info Text
                    Text(
                        text = "Your birth date helps us provide more accurate astrological insights and personalized tarot readings",
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileCompletionScreenPreview() {
    TarotTheme {
        ProfileCompletionScreen()
    }
}

@Preview(showBackground = true, name = "Profile Completion - Dark Theme")
@Composable
fun ProfileCompletionScreenDarkPreview() {
    TarotTheme(darkTheme = true) {
        ProfileCompletionScreen()
    }
}