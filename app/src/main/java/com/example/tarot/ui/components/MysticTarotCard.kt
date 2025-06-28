package com.example.tarot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarot.data.model.TarotCard
import com.example.tarot.ui.theme.CardBorder
import com.example.tarot.ui.theme.MysticDarkBlue
import com.example.tarot.ui.theme.MysticLightGold
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.ui.theme.TextPrimary
import com.example.tarot.util.ImageResourceMapper

/**
 * Reusable Tarot Card Back component
 */
@Composable
fun MysticCardBack(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(ImageResourceMapper.getCardBackResource()),
            contentDescription = "Tarot Card Back",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Tarot Card Front component that displays actual JPG images
 */
@Composable
fun MysticCardFront(
    tarotCard: TarotCard,
    isReversed: Boolean = false,
    modifier: Modifier = Modifier
) {
    val imageResource = ImageResourceMapper.getCardImageResource(tarotCard.imageName)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .graphicsLayer {
                // Only apply the base flip for card reveal - rotation for reversed is separate
                rotationY = 180f
                // Apply reversed rotation only when card is actually reversed
                rotationZ = if (isReversed) 180f else 0f
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(imageResource),
            contentDescription = "${tarotCard.name}${if (isReversed) " (Reversed)" else ""}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Add visual indicator for reversed cards
        if (isReversed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = "R",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MysticLightGold,
                    modifier = Modifier
                        .background(
                            color = MysticDarkBlue.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Legacy version - keeping for backward compatibility
 */
@Composable
fun MysticCardFront(
    cardName: String,
    cardImage: String,
    isReversed: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MysticLightGold,
                        TextPrimary
                    )
                )
            )
            .border(
                width = 3.dp,
                color = CardBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .graphicsLayer {
                // Base flip to show correctly, plus additional 180° if reversed
                rotationY = 180f
                rotationZ = if (isReversed) 180f else 0f
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = cardImage,
                fontSize = 72.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = cardName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MysticDarkBlue,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true, name = "Card Back Preview")
@Composable
fun MysticCardBackPreview() {
    TarotTheme {
        Box(
            modifier = Modifier
                .size(220.dp, 350.dp)
                .background(MysticDarkBlue),
            contentAlignment = Alignment.Center
        ) {
            MysticCardBack()
        }
    }
}

@Preview(showBackground = true, name = "Card Front Preview")
@Composable
fun MysticCardFrontPreview() {
    TarotTheme {
        Box(
            modifier = Modifier
                .size(220.dp, 350.dp)
                .background(MysticDarkBlue),
            contentAlignment = Alignment.Center
        ) {
            MysticCardFront("The Sun", "☀️")
        }
    }
}
