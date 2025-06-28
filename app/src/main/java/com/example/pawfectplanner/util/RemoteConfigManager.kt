package com.example.pawfectplanner.util

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.tasks.await

object RemoteConfigManager {
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    fun init() {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    suspend fun fetchAndActivate() {
        val result = remoteConfig.fetchAndActivate().await()
        Log.d("RemoteConfig", "Fetch result: $result")
        Log.d("RemoteConfig", "Gemini key: ${getGeminiApiKey()}")
        Log.d("RemoteConfig", "Pets key: ${getPetsApiKey()}")
    }

    fun getGeminiApiKey(): String = remoteConfig.getString("gemini_api_key")
    fun getPetsApiKey(): String = remoteConfig.getString("pets_api_key")
}
