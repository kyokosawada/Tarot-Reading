package com.example.tarot.data.repository

import android.util.Log
import com.example.tarot.data.api.OpenAiApiService
import com.example.tarot.data.model.ChatMessage
import com.example.tarot.data.model.OpenAiRequest
import com.example.tarot.data.model.PalmContent
import com.example.tarot.data.model.PalmImageUrl
import com.example.tarot.data.model.PalmMessage
import com.example.tarot.data.model.PalmReadingRequest
import com.example.tarot.util.ApiKeyManager
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PalmReadingRepository @Inject constructor(
    private val apiService: OpenAiApiService,
    private val apiKeyManager: ApiKeyManager
) {
    private val tag = "PalmReadingRepository"

    suspend fun analyzePalmImage(imageBase64: String): Result<String> {
        return try {
            if (!apiKeyManager.isApiKeyConfigured()) {
                return Result.failure(Exception("OpenAI API key not configured"))
            }

            // Step 1: Get educational analysis of the palm image
            Log.d(tag, "Step 1: Getting educational analysis...")
            val educationalRequest = createEducationalAnalysisRequest(imageBase64)
            val educationalResponse = apiService.analyzePalmImage(
                authorization = "Bearer ${apiKeyManager.getOpenAiApiKey()}",
                request = educationalRequest
            )

            if (!educationalResponse.isSuccessful) {
                val errorBody = educationalResponse.errorBody()?.string()
                Log.e(tag, "Educational analysis API error: $errorBody")
                return Result.failure(Exception("Failed to analyze palm: ${educationalResponse.message()}"))
            }

            val educationalAnalysis =
                educationalResponse.body()?.choices?.firstOrNull()?.message?.content
                    ?: return Result.failure(Exception("No educational analysis received"))

            Log.d(tag, "Educational analysis received: ${educationalAnalysis.take(200)}...")

            // Step 2: Transform educational analysis into engaging palm reading
            Log.d(tag, "Step 2: Transforming into engaging reading...")
            val engagingRequest = createEngagingReadingOpenAiRequest(educationalAnalysis)
            val engagingResponse = apiService.createChatCompletion(
                authorization = "Bearer ${apiKeyManager.getOpenAiApiKey()}",
                request = engagingRequest
            )

            if (engagingResponse.isSuccessful) {
                val result = engagingResponse.body()?.choices?.firstOrNull()?.message?.content
                    ?: "Unable to create personalized reading"
                Log.d(tag, "Engaging reading created successfully")
                Result.success(result)
            } else {
                val errorBody = engagingResponse.errorBody()?.string()
                Log.e(tag, "Engaging reading API error: $errorBody")
                Result.failure(Exception("Failed to create engaging reading: ${engagingResponse.message()}"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Error analyzing palm", e)
            Result.failure(e)
        }
    }

    private fun createEducationalAnalysisRequest(imageBase64: String): PalmReadingRequest {
        // Validate base64 format for logging/debugging
        val base64Length = imageBase64.length
        Log.d(tag, "Base64 image length: $base64Length characters")

        if (base64Length < 100) {
            Log.w(tag, "Base64 image seems too short, might be invalid")
        }

        val preview = if (base64Length > 20) {
            "${imageBase64.take(10)}...${imageBase64.takeLast(10)}"
        } else {
            imageBase64
        }
        Log.d(tag, "Base64 preview: $preview")

        // Compose a prompt solely focused on technical/educational palm image analysis
        val userMessage = PalmMessage(
            role = "user",
            content = listOf(
                PalmContent(
                    type = "text",
                    text = """
Please provide a comprehensive, detailed analysis of the attached image. First, determine if this image contains a human hand/palm.

If it IS a hand/palm, provide an extremely detailed analysis covering:

**MAJOR PALM LINES:**
1. **Life Line**: Location, length, depth, curvature, any breaks or chains, starting/ending points
2. **Heart Line**: Position, depth, direction, branches, relationship to fingers
3. **Head Line**: Clarity, length, slope, connection to life line, any islands or breaks
4. **Fate Line**: Presence/absence, strength, direction, intersections with other lines

**MINOR LINES & MARKINGS:**
5. **Sun Line (Apollo Line)**: Visibility, strength, position
6. **Mercury Line (Health Line)**: Presence, clarity, any interruptions
7. **Marriage Lines**: Number, depth, position on palm edge
8. **Travel Lines**: Any visible horizontal lines on palm edge
9. **Special Markings**: Stars, triangles, squares, crosses, islands, dots

**HAND SHAPE & STRUCTURE:**
10. **Palm Shape**: Square, rectangular, or tapered
11. **Finger Proportions**: Relative lengths of index, middle, ring, pinky
12. **Finger Shapes**: Pointed, square, spatulate tips
13. **Thumb Analysis**: Size, position, flexibility, joints
14. **Nail Analysis**: Shape, size, condition

**MOUNTS ANALYSIS:**
15. **Mount of Venus** (base of thumb): Size, firmness, lines
16. **Mount of Jupiter** (below index): Prominence, markings
17. **Mount of Saturn** (below middle): Development, lines
18. **Mount of Apollo** (below ring): Height, characteristics
19. **Mount of Mercury** (below pinky): Size, markings
20. **Mount of Mars** (both upper and lower): Firmness, development
21. **Mount of Luna** (opposite thumb): Size, texture, lines

**TEXTURE & COLOR:**
22. **Skin Texture**: Smooth, rough, soft, firm
23. **Color Variations**: Any unusual coloring or patterns
24. **Flexibility**: Apparent stiffness or suppleness

**OVERALL OBSERVATIONS:**
25. **Hand Dominance**: Which hand appears to be captured
26. **Age Indicators**: Visible wear patterns, line development
27. **Unique Features**: Any unusual characteristics or rare markings

If it is NOT a hand/palm:
- State "NOT_HAND_IMAGE" and describe what you observe instead.

Provide technical, objective observations only. No interpretations or personality assessments.
                    """.trimIndent()
                ),
                PalmContent(
                    type = "image_url",
                    imageUrl = PalmImageUrl(
                        url = "data:image/jpeg;base64,$imageBase64"
                    )
                )
            )
        )

        val request = PalmReadingRequest(
            messages = listOf(userMessage)
        )

        // Debug logging of JSON serialization
        val gson = Gson()
        val jsonString = gson.toJson(request)
        Log.d(tag, "EducationalAnalysisRequest JSON length: ${jsonString.length}")
        Log.d(tag, "EducationalAnalysisRequest JSON preview: ${jsonString.take(500)}...")

        return request
    }

    private fun createEngagingReadingOpenAiRequest(educationalAnalysis: String): OpenAiRequest {
        val systemPrompt = """
You're a mystical palmist and cosmic guide in a tarot and divination app. 

IMPORTANT: Always start your response with "Hello, fellow mystic! ✨".

Based on the technical analysis provided, respond as follows:

If the analysis indicates this IS a hand/palm:
- Create an engaging, warm palm reading
- Summarize the anatomical features in accessible, mystical language
- Offer personality and character insights from palmistry traditions
- Keep the tone encouraging, positive, and spiritually uplifting
- End with mystical blessing or encouragement

If the analysis indicates this is NOT a hand (contains "NOT_HAND_IMAGE"):
- Gently acknowledge this isn't a palm image
- Provide brief educational information about palmistry traditions
- Explain what palm readers typically look for in hands
- Suggest capturing a clear palm image for a proper reading
- Keep the mystical, encouraging tone
- Offer the user to take another photo of the hand and make it clearer

Technical analysis:
$educationalAnalysis
        """.trimIndent()

        val systemMessage = ChatMessage(
            role = "system",
            content = systemPrompt
        )

        val userMessage = ChatMessage(
            role = "user",
            content = "Create an engaging, culturally rich palm reading based on the analysis above."
        )

        val request = OpenAiRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(systemMessage, userMessage),
            maxTokens = 800,
            temperature = 0.8
        )

        // Debug logging of JSON serialization
        val gson = Gson()
        val jsonString = gson.toJson(request)
        Log.d(tag, "EngagingReadingOpenAiRequest JSON length: ${jsonString.length}")
        Log.d(tag, "EngagingReadingOpenAiRequest JSON preview: ${jsonString.take(500)}...")

        return request
    }
}