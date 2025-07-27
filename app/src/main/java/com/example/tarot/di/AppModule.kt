package com.example.tarot.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.tarot.data.FirebaseRepository
import com.example.tarot.data.api.OpenAiApiService
import com.example.tarot.data.dao.DailyReadingDao
import com.example.tarot.data.dao.TarotCardDao
import com.example.tarot.data.database.TarotDatabase
import com.example.tarot.data.repository.JourneyRepository
import com.example.tarot.data.repository.OpenAiRepository
import com.example.tarot.data.repository.PalmReadingRepository
import com.example.tarot.data.repository.SettingsRepository
import com.example.tarot.data.repository.TarotRepository
import com.example.tarot.util.ApiKeyManager
import com.example.tarot.viewmodel.AskQuestionViewModel
import com.example.tarot.viewmodel.AuthViewModel
import com.example.tarot.viewmodel.DailyReadingViewModel
import com.example.tarot.viewmodel.HomeViewModel
import com.example.tarot.viewmodel.PalmReadingViewModel
import com.example.tarot.viewmodel.SettingsViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
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

    // DAOs
    single<TarotCardDao> { get<TarotDatabase>().tarotCardDao() }
    single<DailyReadingDao> { get<TarotDatabase>().dailyReadingDao() }

    // Network
    single<HttpLoggingInterceptor> {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single<OkHttpClient> {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .build()
    }

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<OpenAiApiService> {
        get<Retrofit>().create(OpenAiApiService::class.java)
    }

    single { ApiKeyManager }

    // Repositories
    single<TarotRepository> { 
        TarotRepository(
            tarotCardDao = get(),
            dailyReadingDao = get()
        )
    }

    single<FirebaseRepository> { FirebaseRepository() }

    single<OpenAiRepository> { 
        OpenAiRepository(
            openAiApiService = get(),
            tarotRepository = get(),
            context = androidContext()
        )
    }

    single<JourneyRepository> { 
        JourneyRepository(
            firebaseRepository = get(),
            tarotRepository = get()
        )
    }

    single<PalmReadingRepository> { 
        PalmReadingRepository(
            apiService = get(),
            apiKeyManager = get(),
            context = androidContext()
        )
    }

    single<SettingsRepository> { SettingsRepository(androidContext()) }

    // ViewModels
    viewModel<AuthViewModel> { AuthViewModel(get()) }
    viewModel<HomeViewModel> { 
        HomeViewModel(
            firebaseRepository = get(),
            tarotRepository = get(),
            journeyRepository = get()
        )
    }
    
    viewModel<DailyReadingViewModel> { 
        DailyReadingViewModel(
            tarotRepository = get(),
            settingsRepository = get(),
            journeyRepository = get(),
            openAiRepository = get(),
            firebaseRepository = get()
        )
    }
    
    viewModel<AskQuestionViewModel> { 
        AskQuestionViewModel(
            openAiRepository = get(),
            settingsRepository = get(),
            journeyRepository = get(),
            firebaseRepository = get()
        )
    }
    
    viewModel<PalmReadingViewModel> { PalmReadingViewModel(get()) }
    viewModel<SettingsViewModel> { SettingsViewModel(get()) }
}