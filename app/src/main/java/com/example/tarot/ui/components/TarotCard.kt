package com.example.tarot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tarot.ui.theme.TarotTheme
import com.example.tarot.viewmodel.TarotCard as TarotCardData

@Composable
fun TarotCardComponent(
    card: TarotCardData,
    isRevealed: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardModifier = if (onClick != null) {
        modifier.then(
            Modifier.size(width = 120.dp, height = 180.dp)
        )
    } else {
        modifier.size(width = 120.dp, height = 180.dp)
    }

    Card(
        onClick = onClick ?: {},
        modifier = cardModifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRevealed) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        if (isRevealed) {
            RevealedCardContent(card = card)
        } else {
            HiddenCardContent()
        }
    }
}

@Composable
private fun RevealedCardContent(
    card: TarotCardData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            )
            .border(
                width = 2.dp,
                color = if (card.isReversed) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Card number/suit indicator
            if (card.suit != null && card.number != null) {
                Text(
                    text = "${card.number}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            // Card symbol/image placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getCardSymbol(card),
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Card name
            Text(
                text = if (card.isReversed) "${card.name} (R)" else card.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (card.isReversed) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 2,
                modifier = Modifier.height(32.dp)
            )

            // Suit indicator
            if (card.suit != null) {
                Text(
                    text = card.suit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun HiddenCardContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🔮",
                fontSize = 48.sp
            )
            Text(
                text = "Tarot",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun getCardSymbol(card: TarotCardData): String {
    return when (card.name) {
        "The Fool" -> "🃏"
        "The Magician" -> "🎩"
        "The High Priestess" -> "🌙"
        "The Empress" -> "👑"
        "The Emperor" -> "⚔️"
        "The Hierophant" -> "🕊️"
        "The Lovers" -> "💕"
        "The Chariot" -> "🏛️"
        "Strength" -> "🦁"
        "The Hermit" -> "🕯️"
        "Wheel of Fortune" -> "⚡"
        "Justice" -> "⚖️"
        "The Hanged Man" -> "🔄"
        "Death" -> "🦋"
        "Temperance" -> "⚗️"
        "The Devil" -> "😈"
        "The Tower" -> "⚡"
        "The Star" -> "⭐"
        "The Moon" -> "🌙"
        "The Sun" -> "☀️"
        "Judgement" -> "📯"
        "The World" -> "🌍"
        else -> when (card.suit) {
            "Cups" -> "🏆"
            "Wands" -> "🔥"
            "Swords" -> "⚔️"
            "Pentacles" -> "💰"
            else -> "🔮"
        }
    }
}

@Composable
fun TarotCardRow(
    cards: List<TarotCardData>,
    areRevealed: Boolean = true,
    onCardClick: ((TarotCardData) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
    ) {
        cards.forEach { card ->
            TarotCardComponent(
                card = card,
                isRevealed = areRevealed,
                onClick = if (onCardClick != null) {
                    { onCardClick(card) }
                } else null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TarotCardComponentPreview() {
    TarotTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TarotCardComponent(
                card = TarotCardData(
                    id = "1",
                    name = "The Fool",
                    suit = "Major Arcana",
                    number = 0,
                    isReversed = false,
                    meaning = "New beginnings, spontaneity, innocence"
                ),
                isRevealed = true
            )

            TarotCardComponent(
                card = TarotCardData(
                    id = "2",
                    name = "Five of Cups",
                    suit = "Cups",
                    number = 5,
                    isReversed = true,
                    meaning = "Loss, grief, disappointment"
                ),
                isRevealed = false
            )
        }
    }
}