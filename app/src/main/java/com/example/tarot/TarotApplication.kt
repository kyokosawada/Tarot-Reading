package com.example.tarot

import android.app.Application
import com.example.tarot.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TarotApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@TarotApplication)
            modules(appModule)
        }
    }
}