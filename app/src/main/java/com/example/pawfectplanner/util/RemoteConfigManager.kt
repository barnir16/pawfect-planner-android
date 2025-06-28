package com.example.pawfectplanner.util

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.tasks.await

object RemoteConfigManager {
    private val config: FirebaseRemoteConfig = Firebase.remoteConfig

    fun init() {
        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        config.setConfigSettingsAsync(settings)
        config.setDefaultsAsync(
            mapOf(
                "pets_api_key" to "",
                "gemini_api_key" to ""
            )
        )
    }

    suspend fun fetchAndActivate() {
        config.fetchAndActivate().await()
    }

    fun getPetsApiKey(): String =
        config.getString("pets_api_key")

    fun getGeminiApiKey(): String =
        config.getString("gemini_api_key")
}
