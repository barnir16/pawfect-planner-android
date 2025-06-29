package com.example.pawfectplanner.data.repository

import android.util.Log
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.R
import com.example.pawfectplanner.network.NetworkModule
import com.example.pawfectplanner.network.GeminiApiService
import com.example.pawfectplanner.network.GeminiApiService.Content
import com.example.pawfectplanner.network.GeminiApiService.MessagePart
import com.example.pawfectplanner.network.GeminiApiService.GenerateContentRequest
import com.example.pawfectplanner.util.ApiKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class GeminiRepository(
    private val service: GeminiApiService = NetworkModule.geminiService,
    private val app: PawfectPlannerApplication = PawfectPlannerApplication.instance
) {
    companion object {
        private const val MODEL_NAME = "gemini-1.5-flash"
    }

    suspend fun sendMessage(text: String, petContext: String = ""): String = withContext(Dispatchers.IO) {
        val apiKey = ApiKeyManager.geminiApiKey
        if (apiKey.isNullOrBlank()) {
            Log.e("GeminiRepository", "Gemini API key is missing!")
            return@withContext app.getString(R.string.gemini_error_generic)
        }

        val fullPrompt = buildString {
            append(app.getString(R.string.gemini_prompt_introduction))
            if (petContext.isNotBlank()) {
                append("\n")
                append(app.getString(R.string.gemini_prompt_pet_data, petContext))
            }
            append("\n")
            append(app.getString(R.string.gemini_prompt_question, text))
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
                app.getString(R.string.gemini_error_unauthorized)
            HttpURLConnection.HTTP_FORBIDDEN ->
                app.getString(R.string.gemini_error_unauthorized)
            HttpURLConnection.HTTP_UNAVAILABLE,
            429 ->
                app.getString(R.string.gemini_error_generic)
            else ->
                app.getString(R.string.gemini_error_generic)
        }
        is UnknownHostException,
        is SocketTimeoutException ->
            app.getString(R.string.gemini_error_network)
        else ->
            app.getString(R.string.gemini_error_generic)
    }
}
