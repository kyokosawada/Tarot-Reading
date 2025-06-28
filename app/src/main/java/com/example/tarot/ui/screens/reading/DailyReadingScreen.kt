package com.example.tarot.ui.screens.reading

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tarot.data.model.TarotCard
import com.example.tarot.ui.components.MysticCardBack
import com.example.tarot.ui.components.MysticCardFront
import com.example.tarot.ui.theme.MysticDarkBlue
import com.example.tarot.ui.theme.MysticGold
import com.example.tarot.ui.theme.MysticNavy
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.ui.theme.TextAccent
import com.example.tarot.ui.theme.TextPrimary
import com.example.tarot.ui.theme.TextSecondary
import com.example.tarot.viewmodel.DailyReadingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReadingScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: DailyReadingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

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
                        text = "Daily Reading",
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MysticDarkBlue
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MysticDarkBlue,
                            androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.9f),
                            MysticDarkBlue
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
                Spacer(modifier = Modifier.height(16.dp))

                // Daily Reading Title
                Text(
                    text = "Your Daily Guidance",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextAccent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Date
                Text(
                    text = uiState.readingDate,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Instruction text
                if (uiState.dailyCard != null && !uiState.isCardRevealed) {
                    Text(
                        text = "Tap to reveal your daily guidance",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextAccent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else if (uiState.isCardRevealed) {
                    Text(
                        text = "Your guidance for today",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Loading indicator or card display
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MysticGold,
                        modifier = Modifier.padding(32.dp)
                    )
                } else if (uiState.dailyCard != null) {
                    // Tarot Card - always show when card is available
                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .size(width = 220.dp, height = 350.dp)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                if (!uiState.isCardRevealed) {
                                    viewModel.revealCard()
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
                            // Card Front - using actual JPG images
                            uiState.dailyCard?.let { card ->
                                MysticCardFront(
                                    tarotCard = card,
                                    isReversed = uiState.dailyReading?.isReversed ?: false
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Card Interpretation
                if (uiState.isCardRevealed && uiState.dailyCard != null) {
                    uiState.dailyCard?.let { card ->
                        CardInterpretation(
                            card = card,
                            isReversed = uiState.dailyReading?.isReversed ?: false,
                            randomKeywords = viewModel.getRandomKeywords(
                                card,
                                uiState.dailyReading?.isReversed ?: false
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardInterpretation(
    card: TarotCard,
    isReversed: Boolean,
    randomKeywords: List<String>
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
            // Card Name
            Text(
                text = if (isReversed) "${card.name} (Reversed)" else card.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextAccent,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Keywords
            Text(
                text = "Key Insights",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MysticGold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                randomKeywords.forEach { keyword ->
                    Box(
                        modifier = Modifier
                            .wrapContentWidth()
                            .background(
                                color = MysticGold.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = keyword,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Today's Message
            Text(
                text = "Today's Message",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MysticGold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = if (isReversed) card.reversedMeaning else card.dailyMessage,
                fontSize = 14.sp,
                color = TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DailyReadingScreenPreview() {
    TarotTheme {
        DailyReadingScreen()
    }
}
