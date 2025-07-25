package com.example.tarot.data.repository

import android.content.Context
import android.util.Log
import com.example.tarot.data.api.OpenAiApiService
import com.example.tarot.data.model.ChatMessage
import com.example.tarot.data.model.OpenAiRequest
import com.example.tarot.data.model.TarotCard
import com.example.tarot.data.model.TarotReadingResponse
import com.example.tarot.util.ApiKeyManager
import com.example.tarot.util.NetworkUtils
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.random.Random

class OpenAiRepository(
    private val openAiApiService: OpenAiApiService,
    private val tarotRepository: TarotRepository,
    private val context: Context
) {
    companion object {
        private const val TAG = "OpenAiRepository"
    }
    suspend fun getPersonalizedTarotReading(
        question: String,
        apiKey: String = ApiKeyManager.getOpenAiApiKey(),
        allowReversed: Boolean = true
    ): Result<TarotReadingResponse> {
        return try {
            // Check if API key is configured
            if (!ApiKeyManager.isApiKeyConfigured()) {
                Log.w(TAG, "OpenAI API key not configured")
                return Result.failure(Exception("OpenAI API key not configured. Please add your API key to use this feature."))
            }
            
            // Check network connectivity first
            if (!NetworkUtils.isNetworkAvailable(context)) {
                Log.w(TAG, "No network connection")
                return Result.failure(Exception("No internet connection"))
            }
            
            Log.d(TAG, "Starting personalized tarot reading for question: ${question.take(50)}...")
            
            // Get a random tarot card
            val randomCard = tarotRepository.getRandomCard()
            // Randomly determine if card is reversed (50/50 chance) when allowed
            val isReversed = allowReversed && Random.nextBoolean()
            Log.d(
                TAG,
                "Selected card: ${randomCard.name} - ${if (isReversed) "Reversed" else "Upright"} (Allow reversed: $allowReversed)"
            )

            // Create personalized prompt
            val systemPrompt = createSystemPrompt()
            val userPrompt = createUserPrompt(question, randomCard, isReversed)

            val request = OpenAiRequest(
                messages = listOf(
                    ChatMessage("system", systemPrompt),
                    ChatMessage("user", userPrompt)
                )
            )

            Log.d(TAG, "Making API call to OpenAI...")
            val response = openAiApiService.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            if (response.isSuccessful) {
                Log.d(TAG, "API call successful")
                val aiResponse = response.body()
                val content = aiResponse?.choices?.firstOrNull()?.message?.content
                    ?: return Result.failure(Exception("No response from AI"))

                // Parse the AI response to extract structured data
                val tarotReading = parseAiResponse(content, question, randomCard, isReversed)
                Log.d(TAG, "Successfully parsed AI response")

                Result.success(tarotReading)
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "Invalid API key"
                    429 -> "Rate limit exceeded"
                    500, 502, 503 -> "Server unavailable"
                    else -> "API Error: ${response.code()}"
                }
                Log.e(TAG, errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: UnknownHostException) {
            val errorMsg = NetworkUtils.getNetworkErrorMessage(e)
            Log.e(TAG, "Network error - UnknownHostException", e)
            Result.failure(Exception(errorMsg))
        } catch (e: ConnectException) {
            val errorMsg = NetworkUtils.getNetworkErrorMessage(e)
            Log.e(TAG, "Network error - ConnectException", e)
            Result.failure(Exception(errorMsg))
        } catch (e: SocketTimeoutException) {
            val errorMsg = NetworkUtils.getNetworkErrorMessage(e)
            Log.e(TAG, "Network error - SocketTimeoutException", e)
            Result.failure(Exception(errorMsg))
        } catch (e: IOException) {
            val errorMsg = NetworkUtils.getNetworkErrorMessage(e)
            Log.e(TAG, "Network error - IOException", e)
            Result.failure(Exception(errorMsg))
        } catch (e: Exception) {
            val errorMsg = NetworkUtils.getNetworkErrorMessage(e)
            Log.e(TAG, "Unexpected error during tarot reading", e)
            Result.failure(Exception(errorMsg))
        }
    }

    private fun createSystemPrompt(): String {
        return """
            You are a wise and intuitive tarot card reader with decades of experience. You provide personalized, 
            meaningful interpretations that help people gain insight into their situations. Your readings are:
            
            1. Personal and specific to the user's question
            2. Insightful but not overly mystical
            3. Actionable with practical guidance
            4. Empathetic and supportive
            5. Structured with clear sections
            
            Always respond in this exact JSON format:
            {
                "cardMeaning": "Brief explanation of the card's general meaning",
                "personalizedGuidance": "Detailed, personal guidance specific to their question"
            }
            
            Keep cardMeaning to 2-3 sentences and personalizedGuidance to 3-4 sentences.
        """.trimIndent()
    }

    private fun createUserPrompt(question: String, card: TarotCard, isReversed: Boolean): String {
        val cardMeaning = if (isReversed) card.reversedMeaning else card.uprightMeaning
        val orientation = if (isReversed) "Reversed" else "Upright"

        return """
            The user asks: "$question"
            
            The drawn tarot card is: ${card.name} ($orientation)
            Card Description: ${card.description}
            ${orientation} Meaning: $cardMeaning
            
            Please provide a personalized tarot reading in the specified JSON format.
        """.trimIndent()
    }

    private fun parseAiResponse(
        content: String,
        question: String,
        card: TarotCard,
        isReversed: Boolean
    ): TarotReadingResponse {
        return try {
            // Try to extract JSON from the response
            val jsonStart = content.indexOf("{")
            val jsonEnd = content.lastIndexOf("}") + 1

            if (jsonStart != -1 && jsonEnd > jsonStart) {
                val jsonContent = content.substring(jsonStart, jsonEnd)
                // Simple JSON parsing (you could use Gson here for more robustness)
                val cardMeaning = extractJsonValue(jsonContent, "cardMeaning")
                val personalizedGuidance = extractJsonValue(jsonContent, "personalizedGuidance")

                val fallbackMeaning = if (isReversed) card.reversedMeaning else card.uprightMeaning

                TarotReadingResponse(
                    cardName = card.name,
                    cardMeaning = cardMeaning.ifEmpty { fallbackMeaning },
                    personalizedGuidance = personalizedGuidance.ifEmpty {
                        createFallbackGuidance(question, card, isReversed)
                    },
                    question = question,
                    tarotCard = card, // Include the full card object
                    isReversed = isReversed
                )
            } else {
                // Fallback if JSON parsing fails
                createFallbackReading(content, question, card, isReversed)
            }
        } catch (e: Exception) {
            // Fallback reading
            createFallbackReading(content, question, card, isReversed)
        }
    }

    private fun extractJsonValue(json: String, key: String): String {
        val pattern = """"$key"\s*:\s*"([^"]+)"""".toRegex()
        return pattern.find(json)?.groupValues?.get(1) ?: ""
    }

    private fun createFallbackReading(
        content: String,
        question: String,
        card: TarotCard,
        isReversed: Boolean
    ): TarotReadingResponse {
        val cardMeaning = if (isReversed) card.reversedMeaning else card.uprightMeaning

        return TarotReadingResponse(
            cardName = card.name,
            cardMeaning = cardMeaning,
            personalizedGuidance = content.take(300) + "...",
            question = question,
            tarotCard = card, // Include the full card object
            isReversed = isReversed
        )
    }

    private fun createFallbackGuidance(
        question: String,
        card: TarotCard,
        isReversed: Boolean
    ): String {
        val cardMeaning = if (isReversed) card.reversedMeaning else card.uprightMeaning
        val orientation = if (isReversed) "reversed" else "upright"

        return "The ${card.name} appears ${orientation} in response to your question about ${question.lowercase()}. " +
                "$cardMeaning Consider how this energy relates to your current situation and " +
                "trust your intuition to guide you forward."
    }
}