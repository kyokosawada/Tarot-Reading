package com.example.tarot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.tarot.ui.theme.CardBack
import com.example.tarot.ui.theme.CardBorder
import com.example.tarot.ui.theme.MysticDarkBlue
import com.example.tarot.ui.theme.MysticGold
import com.example.tarot.ui.theme.MysticLightGold
import com.example.tarot.ui.theme.MysticNavy
import com.example.tarot.ui.theme.MysticSilver
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.ui.theme.TextAccent
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
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CardBack,
                        MysticNavy
                    )
                )
            )
            .border(
                width = 3.dp,
                color = CardBorder,
                shape = RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🌟",
                fontSize = 48.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "MYSTICA",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextAccent,
                letterSpacing = 4.sp
            )

            Box(
                modifier = Modifier
                    .size(40.dp, 2.dp)
                    .background(MysticGold)
                    .padding(vertical = 8.dp)
            )

            Text(
                text = "✨ ◊ ✨",
                fontSize = 16.sp,
                color = MysticSilver,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

/**
 * Tarot Card Front component that displays actual JPG images
 */
@Composable
fun MysticCardFront(
    tarotCard: TarotCard,
    modifier: Modifier = Modifier
) {
    val imageResource = ImageResourceMapper.getCardImageResource(tarotCard.imageName)

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
                rotationY = 180f // Flip to show correctly
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card Image (takes most of the space)
            Image(
                painter = painterResource(imageResource),
                contentDescription = tarotCard.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // Card Name
            Text(
                text = tarotCard.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MysticDarkBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
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
                rotationY = 180f // Flip to show correctly
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
