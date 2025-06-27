package com.example.tarot.ui.screens.reading

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tarot.data.model.TarotReadingResponse
import com.example.tarot.ui.components.MysticCardBack
import com.example.tarot.ui.components.MysticCardFront
import com.example.tarot.ui.theme.BackgroundEnd
import com.example.tarot.ui.theme.BackgroundStart
import com.example.tarot.ui.theme.MysticDarkBlue
import com.example.tarot.ui.theme.MysticGold
import com.example.tarot.ui.theme.MysticNavy
import com.example.tarot.ui.theme.MysticPurple
import com.example.tarot.ui.theme.MysticSilver
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.ui.theme.TextAccent
import com.example.tarot.ui.theme.TextPrimary
import com.example.tarot.ui.theme.TextSecondary
import com.example.tarot.viewmodel.AskQuestionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskQuestionScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AskQuestionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var question by remember { mutableStateOf("") }

    val cardRotation by animateFloatAsState(
        targetValue = if (uiState.isCardRevealed) 180f else 0f,
        animationSpec = tween(800),
        label = "cardFlip"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ask a Question",
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
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Show error if present
                uiState.error?.let { error ->
                    ErrorCard(error = error)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Question Section or Loading
                when {
                    uiState.isLoading -> {
                        LoadingSection()
                    }
                    !uiState.hasAskedQuestion -> {
                        QuestionInput(
                            question = question,
                            onQuestionChange = { question = it },
                            onAskQuestion = {
                                if (question.isNotBlank()) {
                                    viewModel.askQuestion(question)
                                }
                            }
                        )
                    }

                    else -> {
                        // Show the question that was asked
                        QuestionDisplay(question = uiState.reading?.question ?: question)

                        // Reading Title
                        Text(
                            text = "The Universe Responds",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextAccent,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Tap the card to reveal your guidance",
                            fontSize = 16.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 48.dp)
                        )

                        // Tarot Card
                        Box(
                            modifier = Modifier
                                .size(width = 220.dp, height = 350.dp)
                                .clickable {
                                    if (!uiState.isCardRevealed) {
                                        viewModel.setCardRevealed(true)
                                        viewModel.setShowInterpretation(true)
                                    }
                                }
                                .graphicsLayer {
                                    rotationY = cardRotation
                                    cameraDistance = 12f * density
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (cardRotation <= 90f) {
                                // Card Back
                                MysticCardBack()
                            } else {
                                // Card Front with actual JPG image
                                uiState.reading?.tarotCard?.let { tarotCard ->
                                    MysticCardFront(tarotCard = tarotCard)
                                } ?: run {
                                    // Fallback to emoji version if no card data
                                    MysticCardFront(
                                        cardName = uiState.reading?.cardName
                                            ?: "The High Priestess",
                                        cardImage = getCardEmoji(
                                            uiState.reading?.cardName ?: "The High Priestess"
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }

                // Card Interpretation from AI
                if (uiState.showInterpretation && uiState.reading != null) {
                    AiCardInterpretation(
                        reading = uiState.reading!!,
                        onAskAnother = {
                            question = ""
                            viewModel.resetReading()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionInput(
    question: String,
    onQuestionChange: (String) -> Unit,
    onAskQuestion: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What would you like guidance on?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextAccent,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Focus your mind and ask your question. The cards will provide insight and guidance.",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Question Input Field
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
                OutlinedTextField(
                    value = question,
                    onValueChange = onQuestionChange,
                    label = { Text("Your Question", color = TextSecondary) },
                    placeholder = {
                        Text(
                            "e.g., What should I focus on in my career?",
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MysticGold,
                        unfocusedBorderColor = MysticSilver,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = MysticGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onAskQuestion,
                    enabled = question.isNotBlank(),
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
                    Text(
                        text = "🔮 Ask the Cards",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun AiCardInterpretation(
    reading: TarotReadingResponse,
    onAskAnother: () -> Unit
) {
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
                text = reading.cardName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextAccent,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Card Meaning",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MysticGold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = reading.cardMeaning,
                fontSize = 14.sp,
                color = TextPrimary,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Guidance",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MysticGold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = reading.personalizedGuidance,
                fontSize = 14.sp,
                color = TextPrimary,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // New Question Button
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onAskAnother,
                modifier = Modifier.fillMaxWidth(),
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
                    text = "Ask Another Question",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun ErrorCard(error: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MysticPurple.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "⚠️ $error",
            fontSize = 14.sp,
            color = TextPrimary,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun LoadingSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        CircularProgressIndicator(
            color = MysticGold,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Consulting the cards...",
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun QuestionDisplay(question: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MysticPurple.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Your Question:",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MysticGold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "\"$question\"",
                fontSize = 16.sp,
                color = TextPrimary,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 22.sp
            )
        }
    }
}

// Helper function to get emoji for card
fun getCardEmoji(cardName: String): String {
    return when {
        cardName.contains("High Priestess", ignoreCase = true) -> "🌙"
        cardName.contains("Fool", ignoreCase = true) -> "🌟"
        cardName.contains("Magician", ignoreCase = true) -> "🔮"
        cardName.contains("Empress", ignoreCase = true) -> "🌸"
        cardName.contains("Emperor", ignoreCase = true) -> "👑"
        cardName.contains("Tower", ignoreCase = true) -> "⚡"
        cardName.contains("Star", ignoreCase = true) -> "⭐"
        cardName.contains("Sun", ignoreCase = true) -> "☀️"
        cardName.contains("Moon", ignoreCase = true) -> "🌙"
        cardName.contains("Death", ignoreCase = true) -> "🦋"
        cardName.contains("Strength", ignoreCase = true) -> "🦁"
        cardName.contains("Justice", ignoreCase = true) -> "⚖️"
        cardName.contains("Temperance", ignoreCase = true) -> "🌊"
        cardName.contains("Devil", ignoreCase = true) -> "😈"
        cardName.contains("Judgement", ignoreCase = true) -> "📯"
        cardName.contains("World", ignoreCase = true) -> "🌍"
        else -> "🔮"
    }
}

@Preview(showBackground = true)
@Composable
fun AskQuestionScreenPreview() {
    TarotTheme {
        AskQuestionScreen()
    }
}
