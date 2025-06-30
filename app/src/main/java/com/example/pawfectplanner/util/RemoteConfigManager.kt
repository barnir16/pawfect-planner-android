package com.example.pawfectplanner.util

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await

object RemoteConfigManager {

    fun init() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(settings)
    }

    suspend fun fetchAndActivate() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val activated = remoteConfig.fetchAndActivate().await()
        Log.d("RemoteConfig", "Fetch result: $activated")
        Log.d("RemoteConfig", "Gemini key: ${getGeminiApiKey()}")
        Log.d("RemoteConfig", "Pets key: ${getPetsApiKey()}")
    }

    fun getGeminiApiKey(): String =
        FirebaseRemoteConfig.getInstance().getString("gemini_api_key")

    fun getPetsApiKey(): String =
        FirebaseRemoteConfig.getInstance().getString("pets_api_key")
}
