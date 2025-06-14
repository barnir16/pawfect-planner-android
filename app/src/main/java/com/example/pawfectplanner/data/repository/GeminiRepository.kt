package com.example.pawfectplanner.data.repository

import android.content.Context
import com.example.pawfectplanner.network.GeminiApiService
import com.example.pawfectplanner.network.GeminiRequest
import com.example.pawfectplanner.util.ApiKeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class GeminiRepository(context: Context) {
    private val appCtx = context.applicationContext

    private val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun sendMessage(prompt: String): String = withContext(Dispatchers.IO) {
        val key = ApiKeyStore.getGeminiApiKey(appCtx).orEmpty()
        val bearer = "Bearer $key"
        val response = service.generate(bearer, GeminiRequest(prompt))
        response.candidates.firstOrNull()?.content.orEmpty()
    }
}
