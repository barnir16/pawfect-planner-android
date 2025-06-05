package com.example.pawfectplanner.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

object ApiKeyStore {
    private const val PREFS_FILENAME = "secure_api_keys"
    private const val KEY_DOG_API = "dog_api_key"
    private const val KEY_GEMINI_API = "gemini_api_key"

    fun getDogApiKey(context: Context): String? {
        val prefs = getEncryptedPrefs(context)
        return prefs.getString(KEY_DOG_API, null)
    }

    fun setDogApiKey(context: Context, apiKey: String) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit() { putString(KEY_DOG_API, apiKey) }
    }

    fun getGeminiApiKey(context: Context): String? {
        val prefs = getEncryptedPrefs(context)
        return prefs.getString(KEY_GEMINI_API, null)
    }

    fun setGeminiApiKey(context: Context, apiKey: String) {
        val prefs = getEncryptedPrefs(context)
        prefs.edit() { putString(KEY_GEMINI_API, apiKey) }
    }

    private fun getEncryptedPrefs(context: Context) =
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILENAME,
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
}
