package com.example.tarot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.tarot.data.model.CardType
import com.example.tarot.data.model.Suit
import com.example.tarot.data.model.TarotCard
import kotlinx.coroutines.flow.Flow

@Dao
interface TarotCardDao {

    @Query("SELECT * FROM tarot_cards ORDER BY id ASC")
    fun getAllCards(): Flow<List<TarotCard>>

    @Query("SELECT * FROM tarot_cards WHERE cardType = :cardType ORDER BY id ASC")
    fun getCardsByType(cardType: CardType): Flow<List<TarotCard>>

    @Query("SELECT * FROM tarot_cards WHERE suit = :suit ORDER BY numerology ASC")
    fun getCardsBySuit(suit: Suit): Flow<List<TarotCard>>

    @Query("SELECT * FROM tarot_cards WHERE id = :id")
    suspend fun getCardById(id: Int): TarotCard?

    @Query("SELECT * FROM tarot_cards WHERE name = :name")
    suspend fun getCardByName(name: String): TarotCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<TarotCard>)

    @Query("SELECT COUNT(*) FROM tarot_cards")
    suspend fun getCardCount(): Int

    @Query("SELECT * FROM tarot_cards ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomCard(): TarotCard

    @Query("SELECT * FROM tarot_cards ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomCards(count: Int): List<TarotCard>
}