package com.example.tarot.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.tarot.data.dao.DailyReadingDao
import com.example.tarot.data.dao.TarotCardDao
import com.example.tarot.data.model.CardType
import com.example.tarot.data.model.DailyReading
import com.example.tarot.data.model.Suit
import com.example.tarot.data.model.TarotCard

@Database(
    entities = [TarotCard::class, DailyReading::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TarotDatabase : RoomDatabase() {

    abstract fun tarotCardDao(): TarotCardDao
    abstract fun dailyReadingDao(): DailyReadingDao


}

class Converters {
    @TypeConverter
    fun fromSuit(suit: Suit?): String? {
        return suit?.name
    }

    @TypeConverter
    fun toSuit(suitName: String?): Suit? {
        return suitName?.let { Suit.valueOf(it) }
    }

    @TypeConverter
    fun fromCardType(cardType: CardType): String {
        return cardType.name
    }

    @TypeConverter
    fun toCardType(cardTypeName: String): CardType {
        return CardType.valueOf(cardTypeName)
    }
}