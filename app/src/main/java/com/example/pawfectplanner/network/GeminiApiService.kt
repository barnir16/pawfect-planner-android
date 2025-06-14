package com.example.pawfectplanner.network

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class GeminiRequest(val prompt: String)
data class GeminiResponse(val candidates: List<Candidate>) {
    data class Candidate(val content: String)
}

interface GeminiApiService {
    @POST("v1/chat:generate")
    suspend fun generate(
        @Header("Authorization") bearer: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}
