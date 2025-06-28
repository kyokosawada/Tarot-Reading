package com.example.tarot.data.repository

import com.example.tarot.data.dao.DailyReadingDao
import com.example.tarot.data.dao.TarotCardDao
import com.example.tarot.data.model.CardType
import com.example.tarot.data.model.DailyReading
import com.example.tarot.data.model.Suit
import com.example.tarot.data.model.TarotCard
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TarotRepository @Inject constructor(
    private val tarotCardDao: TarotCardDao,
    private val dailyReadingDao: DailyReadingDao
) {

    // Cache initialization status to avoid repeated DB checks
    private var isInitialized = false

    // Initialize database on first access if needed
    private suspend fun ensureDatabaseInitialized() {
        if (!isInitialized && isDatabaseEmpty()) {
            try {
                val allTarotCards = TarotCardData.getAllTarotCards()
                insertCards(allTarotCards)
                isInitialized = true
            } catch (e: Exception) {
                throw IllegalStateException("Failed to initialize tarot database", e)
            }
        }
    }

    fun getAllCards(): Flow<List<TarotCard>> = tarotCardDao.getAllCards()

    fun getCardsByType(cardType: CardType): Flow<List<TarotCard>> =
        tarotCardDao.getCardsByType(cardType)

    fun getCardsBySuit(suit: Suit): Flow<List<TarotCard>> =
        tarotCardDao.getCardsBySuit(suit)

    suspend fun getCardById(id: Int): TarotCard? {
        ensureDatabaseInitialized()
        return tarotCardDao.getCardById(id)
    }

    suspend fun getCardByName(name: String): TarotCard? {
        ensureDatabaseInitialized()
        return tarotCardDao.getCardByName(name)
    }

    suspend fun getRandomCard(): TarotCard {
        ensureDatabaseInitialized()
        return tarotCardDao.getRandomCard()
    }

    suspend fun getRandomCards(count: Int): List<TarotCard> {
        ensureDatabaseInitialized()

        val totalCount = tarotCardDao.getCardCount()
        if (count > totalCount) throw IllegalArgumentException("Requested more cards than available")

        return tarotCardDao.getRandomCards(count)
    }

    suspend fun insertCards(cards: List<TarotCard>) = tarotCardDao.insertCards(cards)

    suspend fun isDatabaseEmpty(): Boolean = tarotCardDao.getCardCount() == 0

    // Daily Reading methods
    suspend fun getTodaysReading(): DailyReading? {
        val todayDate = getTodayDateString()
        return dailyReadingDao.getDailyReadingByDate(todayDate)
    }

    suspend fun saveDailyReading(card: TarotCard, isReversed: Boolean = false): DailyReading {
        val todayDate = getTodayDateString()
        val dailyReading = DailyReading(
            date = todayDate,
            cardId = card.id,
            cardName = card.name,
            isRevealed = false,
            isReversed = isReversed
        )
        dailyReadingDao.insertDailyReading(dailyReading)
        return dailyReading
    }

    suspend fun updateDailyReading(dailyReading: DailyReading) {
        dailyReadingDao.updateDailyReading(dailyReading)
    }

    suspend fun getAllDailyReadings(): List<DailyReading> {
        return dailyReadingDao.getAllDailyReadings()
    }

    suspend fun cleanupOldReadings(daysToKeep: Int = 30) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -daysToKeep)
        val cutoffDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        dailyReadingDao.deleteOldReadings(cutoffDate)
    }

    private fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}