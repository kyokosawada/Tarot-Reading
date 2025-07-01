package com.example.tarot

import android.app.Application
import com.example.tarot.util.CameraUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TarotApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Clean up any old palm reading images on app startup
        CameraUtils.cleanupOldPalmImages(this)
    }
}