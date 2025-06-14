package com.example.pawfectplanner.data.repository

import android.content.Context
import com.example.pawfectplanner.network.GenerateMessageRequest
import com.example.pawfectplanner.network.MessagePrompt
import com.example.pawfectplanner.network.GeminiApiService
import com.example.pawfectplanner.util.ApiKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class GeminiRepository(context: Context) {
    private val ctx = context.applicationContext
    private val service by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun sendMessage(text: String): String = withContext(Dispatchers.IO) {
        val apiKey = ApiKeyStore.getGeminiApiKey(ctx).orEmpty()
        require(apiKey.isNotBlank()) { "Gemini API key not set" }
        val bearer = "Bearer $apiKey"
        val request = GenerateMessageRequest(prompt = MessagePrompt(text))
        val response = service.generateMessage(MODEL_NAME, bearer, request)
        response.candidates.firstOrNull()?.content.orEmpty()
    }

    companion object {
        private const val MODEL_NAME = "gemini-2.0-flash"
    }
}
