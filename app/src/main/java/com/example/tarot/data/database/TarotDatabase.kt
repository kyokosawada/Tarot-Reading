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
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class TarotDatabase : RoomDatabase() {

    abstract fun tarotCardDao(): TarotCardDao
    abstract fun dailyReadingDao(): DailyReadingDao

    companion object {
        @Volatile
        private var INSTANCE: TarotDatabase? = null

        fun getDatabase(context: Context): TarotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TarotDatabase::class.java,
                    "tarot_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
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