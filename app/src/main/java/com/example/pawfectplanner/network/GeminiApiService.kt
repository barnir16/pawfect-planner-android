package com.example.pawfectplanner.network

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface GeminiApiService {
    @JsonClass(generateAdapter = true)
    data class MessagePrompt(val text: String)

    @JsonClass(generateAdapter = true)
    data class GenerateMessageRequest(val prompt: MessagePrompt)

    @JsonClass(generateAdapter = true)
    data class GenerateMessageResponse(val candidates: List<Candidate>) {
        @JsonClass(generateAdapter = true)
        data class Candidate(val content: String)
    }

    @POST("v1/models/{model}:generateMessage")
    suspend fun generateMessage(
        @Path("model") model: String,
        @Header("Authorization") authorization: String,
        @Body request: GenerateMessageRequest
    ): GenerateMessageResponse
}
