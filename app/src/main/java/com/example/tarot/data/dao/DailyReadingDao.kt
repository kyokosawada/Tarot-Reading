package com.example.tarot.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.tarot.data.model.DailyReading

@Dao
interface DailyReadingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyReading(dailyReading: DailyReading)

    @Update
    suspend fun updateDailyReading(dailyReading: DailyReading)

    @Query("SELECT * FROM daily_readings WHERE date = :date LIMIT 1")
    suspend fun getDailyReadingByDate(date: String): DailyReading?

    @Query("SELECT * FROM daily_readings ORDER BY date DESC")
    suspend fun getAllDailyReadings(): List<DailyReading>

    @Query("DELETE FROM daily_readings WHERE date < :cutoffDate")
    suspend fun deleteOldReadings(cutoffDate: String)
}