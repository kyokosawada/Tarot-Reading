package com.example.tarot.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_readings")
data class DailyReading(
    @PrimaryKey
    val date: String, // Format: "yyyy-MM-dd"
    val cardId: Int,
    val cardName: String,
    val isRevealed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)