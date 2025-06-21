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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarot.ui.components.MysticCardBack
import com.example.tarot.ui.components.MysticCardFront
import com.example.tarot.ui.theme.BackgroundEnd
import com.example.tarot.ui.theme.BackgroundStart
import com.example.tarot.ui.theme.MysticDarkBlue
import com.example.tarot.ui.theme.MysticGold
import com.example.tarot.ui.theme.MysticNavy
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.ui.theme.TextAccent
import com.example.tarot.ui.theme.TextPrimary
import com.example.tarot.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReadingScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isCardRevealed by remember { mutableStateOf(false) }
    var showInterpretation by remember { mutableStateOf(false) }

    val cardRotation by animateFloatAsState(
        targetValue = if (isCardRevealed) 180f else 0f,
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

                // Daily Reading Title
                Text(
                    text = "Your Daily Guidance",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextAccent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Tap the card to reveal your message",
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
                            if (!isCardRevealed) {
                                isCardRevealed = true
                                showInterpretation = true
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
                        // Card Front - The Sun card as example
                        MysticCardFront(
                            cardName = "The Sun",
                            cardImage = "☀️"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Card Interpretation
                if (showInterpretation) {
                    CardInterpretation()
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun CardInterpretation() {
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
                text = "The Sun",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextAccent,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Meaning",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MysticGold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "The Sun represents joy, success, celebration, and positivity. This card brings optimism and good fortune to your day. You are entering a period of happiness and achievement.",
                fontSize = 14.sp,
                color = TextPrimary,
                lineHeight = 20.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Today's Message",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MysticGold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Embrace the positive energy around you. Share your light with others and watch how it multiplies. Success is within reach - trust in your abilities and let your authentic self shine through.",
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
