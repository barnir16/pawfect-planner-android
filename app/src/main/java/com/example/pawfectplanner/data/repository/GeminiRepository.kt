package com.example.pawfectplanner.data.repository

import com.example.pawfectplanner.network.NetworkModule
import com.example.pawfectplanner.network.GeminiApiService
import com.example.pawfectplanner.network.GeminiApiService.GenerateMessageRequest
import com.example.pawfectplanner.network.GeminiApiService.MessagePrompt
import com.example.pawfectplanner.util.ApiKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository(
    private val service: GeminiApiService = NetworkModule.geminiService
) {
    companion object {
        private const val MODEL_NAME = "gemini-2.0-flash"
    }

    suspend fun sendMessage(text: String): String = withContext(Dispatchers.IO) {
        val apiKey = ApiKeyManager.geminiApiKey ?: error("Gemini API key not loaded")
        val bearer = "Bearer $apiKey"
        val request = GenerateMessageRequest(
            prompt = MessagePrompt(text)
        )
        val response = service.generateMessage(MODEL_NAME, bearer, request)
        response.candidates.firstOrNull()?.content.orEmpty()
    }
}
