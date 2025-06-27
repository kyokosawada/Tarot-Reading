package com.example.tarot.util

import com.example.tarot.BuildConfig

object ApiKeyManager {

    private val OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY

    fun getOpenAiApiKey(): String {
        return OPENAI_API_KEY
    }

    fun isApiKeyConfigured(): Boolean {
        return OPENAI_API_KEY.isNotBlank() &&
                OPENAI_API_KEY.startsWith("sk-")
    }
}