package com.example.pawfectplanner.data.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.pawfectplanner.network.GeminiApiService
import com.example.pawfectplanner.network.GeminiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@RequiresApi(Build.VERSION_CODES.M)
class GeminiRepository(private val context: Context) {

    private val prefs by lazy {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "api_keys_prefs",
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val service by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun sendMessage(prompt: String): String = withContext(Dispatchers.IO) {
        val key = prefs.getString("gemini_api_key", "")!!
        val bearer = "Bearer $key"
        val resp = service.generate(bearer, GeminiRequest(prompt))
        resp.candidates.firstOrNull()?.content.orEmpty()
    }
}
