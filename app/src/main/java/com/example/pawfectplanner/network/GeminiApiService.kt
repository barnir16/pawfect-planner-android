package com.example.pawfectplanner.network

import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GeminiApiService {
    @JsonClass(generateAdapter = true)
    data class MessagePart(val text: String)

    @JsonClass(generateAdapter = true)
    data class Content(val parts: List<MessagePart>)

    @JsonClass(generateAdapter = true)
    data class GenerateContentRequest(val contents: List<Content>)

    @JsonClass(generateAdapter = true)
    data class GenerateContentResponse(val candidates: List<Candidate>) {
        @JsonClass(generateAdapter = true)
        data class Candidate(val content: Content?)
    }

    @POST("models/{model}:generateContent")
    suspend fun generateMessage(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}
