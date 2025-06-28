package com.example.pawfectplanner.network

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

@JsonClass(generateAdapter = true)
data class MessagePrompt(val text: String)

data class GenerateMessageRequest(val prompt: MessagePrompt)

data class GenerateMessageResponse(val candidates: List<Candidate>) {
    data class Candidate(val content: String)
}

interface GeminiApiService {
    @POST("v1/models/{model}:generateMessage")
    suspend fun generateMessage(
        @Path("model") model: String,
        @Header("Authorization") authorization: String,
        @Body request: GenerateMessageRequest
    ): GenerateMessageResponse
}
