package com.example.pawfectplanner.data.repository

import android.util.Log
import com.example.pawfectplanner.network.NetworkModule
import com.example.pawfectplanner.network.GeminiApiService
import com.example.pawfectplanner.network.GeminiApiService.Content
import com.example.pawfectplanner.network.GeminiApiService.MessagePart
import com.example.pawfectplanner.network.GeminiApiService.GenerateContentRequest
import com.example.pawfectplanner.util.ApiKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository(
    private val service: GeminiApiService = NetworkModule.geminiService
) {
    companion object {
        private const val MODEL_NAME = "gemini-1.5-flash"
    }

    suspend fun sendMessage(text: String): String = withContext(Dispatchers.IO) {
        val apiKey = ApiKeyManager.geminiApiKey
        if (apiKey.isNullOrBlank()) {
            Log.e("GeminiRepository", "Gemini API key is missing!")
            return@withContext "API key not loaded. Please try again later."
        }

        Log.d("GeminiRequest", "API key length: ${apiKey.length}")
        Log.d("GeminiRequest", "Request text: $text")

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(MessagePart(text))))
        )

        try {
            val response = service.generateMessage(
                MODEL_NAME,
                apiKey,
                request
            )
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
        } catch (e: Exception) {
            Log.e("GeminiRepository", "HTTP error: ${e.message}", e)
            return@withContext "An error occurred: ${e.message}"
        }
    }
}
