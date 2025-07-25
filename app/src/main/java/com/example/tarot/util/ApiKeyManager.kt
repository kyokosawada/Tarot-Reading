package com.example.tarot.util

import com.example.tarot.BuildConfig

object ApiKeyManager {

    private val OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY

    fun getOpenAiApiKey(): String {
        return OPENAI_API_KEY
    }

    fun isApiKeyConfigured(): Boolean {
        return OPENAI_API_KEY.isNotBlank() &&
                (OPENAI_API_KEY.startsWith("sk-") || OPENAI_API_KEY.startsWith("sk-proj-"))
    }

    /**
     * For distribution builds, consider using limited-scope API keys
     * or implementing server-side proxy for enhanced security
     */
    fun isProductionKey(): Boolean {
        return OPENAI_API_KEY.startsWith("sk-proj-") // Project-scoped keys are more secure
    }

    /**
     * Returns obfuscated key for logging (security best practice)
     */
    fun getObfuscatedKey(): String {
        return if (OPENAI_API_KEY.length > 10) {
            "${OPENAI_API_KEY.take(10)}...${OPENAI_API_KEY.takeLast(4)}"
        } else {
            "***"
        }
    }
}