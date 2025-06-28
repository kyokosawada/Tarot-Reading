package com.example.tarot.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tarot.data.FirebaseRepository
import com.example.tarot.data.dao.DailyReadingDao
import com.example.tarot.data.dao.TarotCardDao
import com.example.tarot.data.database.TarotDatabase
import com.example.tarot.data.repository.TarotRepository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TarotDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TarotDatabase::class.java,
            "tarot_database"
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTarotCardDao(database: TarotDatabase): TarotCardDao {
        return database.tarotCardDao()
    }

    @Provides
    fun provideDailyReadingDao(database: TarotDatabase): DailyReadingDao {
        return database.dailyReadingDao()
    }

    @Provides
    @Singleton
    fun provideTarotRepository(
        tarotCardDao: TarotCardDao,
        dailyReadingDao: DailyReadingDao
    ): TarotRepository {
        return TarotRepository(tarotCardDao, dailyReadingDao)
    }

    @Provides
    @Singleton
    fun provideFirebaseRepository(): FirebaseRepository {
        return FirebaseRepository()
    }


}