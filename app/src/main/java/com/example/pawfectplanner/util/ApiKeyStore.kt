package com.example.pawfectplanner.util

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "api_keys_prefs"
private const val DOG_API_KEY = "dog_api_key"
private const val GEMINI_API_KEY = "gemini_api_key"

object ApiKeyStore {
    fun getDogApiKey(context: Context): String? =
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(DOG_API_KEY, null)

    fun setDogApiKey(context: Context, key: String) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(DOG_API_KEY, key)
            }
    }

    fun getGeminiApiKey(context: Context): String? =
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(GEMINI_API_KEY, null)

    fun setGeminiApiKey(context: Context, key: String) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putString(GEMINI_API_KEY, key)
            }
    }
}
