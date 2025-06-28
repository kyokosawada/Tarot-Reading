package com.example.tarot.data.model

import com.google.gson.annotations.SerializedName

// OpenAI Chat Completion Request
data class OpenAiRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<ChatMessage>,
    @SerializedName("max_tokens")
    val maxTokens: Int = 500,
    val temperature: Double = 0.7
)

data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

// OpenAI Chat Completion Response
data class OpenAiResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val index: Int,
    val message: ChatMessage,
    @SerializedName("finish_reason")
    val finishReason: String
)

data class Usage(
    @SerializedName("prompt_tokens")
    val promptTokens: Int,
    @SerializedName("completion_tokens")
    val completionTokens: Int,
    @SerializedName("total_tokens")
    val totalTokens: Int
)

// Tarot Reading Response Model
data class TarotReadingResponse(
    val cardName: String,
    val cardMeaning: String,
    val personalizedGuidance: String,
    val question: String,
    val tarotCard: TarotCard, // Add the full card object for images
    val isReversed: Boolean = false // Track if card is drawn reversed
)

// API Error Response
data class OpenAiError(
    val error: ErrorDetail
)

data class ErrorDetail(
    val message: String,
    val type: String,
    val code: String?
)