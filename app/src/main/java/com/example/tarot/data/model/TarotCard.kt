package com.example.tarot.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tarot_cards",
    indices = [
        Index(value = ["cardType"]),
        Index(value = ["suit"]),
        Index(value = ["name"], unique = true)
    ]
)
data class TarotCard(
    @PrimaryKey val id: Int,
    val name: String,
    val suit: Suit?,
    val cardType: CardType,
    val imageName: String, // Matches drawable filename
    val uprightMeaning: String,
    val reversedMeaning: String,
    val uprightKeywords: String, // Comma-separated keywords
    val reversedKeywords: String, // Comma-separated keywords
    val description: String,
    val uprightDailyMessage: String, // Daily message for upright position
    val reversedDailyMessage: String, // Daily message for reversed position
    val numerology: Int? // For numbered cards (1-10, 11=Page, 12=Knight, 13=Queen, 14=King)
)

enum class CardType {
    MAJOR_ARCANA,
    MINOR_ARCANA
}

enum class Suit {
    CUPS,
    PENTACLES,
    SWORDS,
    WANDS
}

// Helper extension functions for better API (moved outside data class to avoid Room issues)
fun TarotCard.getUprightKeywordsList(): List<String> =
    uprightKeywords.split(",").map { it.trim() }.filter { it.isNotEmpty() }

fun TarotCard.getReversedKeywordsList(): List<String> =
    reversedKeywords.split(",").map { it.trim() }.filter { it.isNotEmpty() }