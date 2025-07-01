package com.example.tarot.data.model

import com.google.gson.annotations.SerializedName

// Palm reading request for OpenAI Vision API
data class PalmReadingRequest(
    val model: String = "gpt-4o",
    val messages: List<PalmMessage>,
    @SerializedName("max_tokens")
    val maxTokens: Int = 1000
)

data class PalmMessage(
    val role: String,
    val content: List<PalmContent>
)

data class PalmContent(
    val type: String, // "text" or "image_url"
    val text: String? = null,
    @SerializedName("image_url")
    val imageUrl: PalmImageUrl? = null
)

data class PalmImageUrl(
    val url: String
)

// Palm reading response model for structured data (future enhancement)
data class PalmReadingResponse(
    val interpretation: String,
    val majorLines: PalmLines,
    val mounts: PalmMounts,
    val overallReading: String,
    val guidance: String
)

data class PalmLines(
    val lifeLine: String,
    val heartLine: String,
    val headLine: String,
    val fateLine: String?
)

data class PalmMounts(
    val venus: String,
    val mars: String,
    val jupiter: String,
    val saturn: String,
    val apollo: String,
    val mercury: String
)