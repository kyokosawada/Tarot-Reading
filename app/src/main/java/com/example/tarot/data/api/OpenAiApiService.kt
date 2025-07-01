package com.example.tarot.data.api

import com.example.tarot.data.model.OpenAiRequest
import com.example.tarot.data.model.OpenAiResponse
import com.example.tarot.data.model.PalmReadingRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAiApiService {
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: OpenAiRequest
    ): Response<OpenAiResponse>

    // Palm reading endpoint using Vision API
    @POST("v1/chat/completions")
    suspend fun analyzePalmImage(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: PalmReadingRequest
    ): Response<OpenAiResponse>

}