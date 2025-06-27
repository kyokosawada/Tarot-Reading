package com.example.tarot.data.repository

import com.example.tarot.data.dao.TarotCardDao
import com.example.tarot.data.model.CardType
import com.example.tarot.data.model.Suit
import com.example.tarot.data.model.TarotCard
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TarotRepository @Inject constructor(
    private val tarotCardDao: TarotCardDao
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
}