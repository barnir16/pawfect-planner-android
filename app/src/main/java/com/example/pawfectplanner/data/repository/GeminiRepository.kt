package com.example.pawfectplanner.data.repository

import android.content.Context
import android.util.Log
import com.example.pawfectplanner.R
import com.example.pawfectplanner.network.GeminiApiService
import com.example.pawfectplanner.network.GeminiApiService.Content
import com.example.pawfectplanner.network.GeminiApiService.MessagePart
import com.example.pawfectplanner.network.GeminiApiService.GenerateContentRequest
import com.example.pawfectplanner.util.ApiKeyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class GeminiRepository @Inject constructor(
    private val service: GeminiApiService,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val MODEL_NAME = "gemini-1.5-flash"
    }

    suspend fun sendMessage(text: String, petContext: String = ""): String = withContext(Dispatchers.IO) {
        val apiKey = ApiKeyManager.geminiApiKey
        if (apiKey.isNullOrBlank()) {
            Log.e("GeminiRepository", "Gemini API key is missing!")
            return@withContext context.getString(R.string.gemini_error_generic)
        }

        val fullPrompt = buildString {
            append(context.getString(R.string.gemini_prompt_introduction))
            if (petContext.isNotBlank()) {
                append("\n")
                append(context.getString(R.string.gemini_prompt_pet_data, petContext))
            }
            append("\n")
            append(context.getString(R.string.gemini_prompt_question, text))
        }

        Log.d("GeminiRequest", "Sending: $fullPrompt")

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(MessagePart(fullPrompt))))
        )

        try {
            val response = service.generateMessage(
                MODEL_NAME,
                apiKey,
                request
            )
            response.candidates
                .firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                .orEmpty()
        } catch (e: Exception) {
            Log.e("GeminiRepository", "HTTP error: ${e.message}", e)
            mapErrorToFriendlyMessage(e)
        }
    }

    private fun mapErrorToFriendlyMessage(e: Exception): String = when (e) {
        is HttpException -> when (e.code()) {
            HttpURLConnection.HTTP_UNAUTHORIZED ->
                context.getString(R.string.gemini_error_unauthorized)
            HttpURLConnection.HTTP_FORBIDDEN ->
                context.getString(R.string.gemini_error_unauthorized)
            HttpURLConnection.HTTP_UNAVAILABLE,
            429 ->
                context.getString(R.string.gemini_error_generic)
            else ->
                context.getString(R.string.gemini_error_generic)
        }
        is UnknownHostException,
        is SocketTimeoutException ->
            context.getString(R.string.gemini_error_network)
        else ->
            context.getString(R.string.gemini_error_generic)
    }
}
