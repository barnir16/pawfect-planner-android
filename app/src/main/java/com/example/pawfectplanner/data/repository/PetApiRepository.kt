package com.example.pawfectplanner.data.repository

import com.example.pawfectplanner.network.NetworkModule
import com.example.pawfectplanner.util.ApiKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PetApiRepository {
    suspend fun getRandomPetImage(animalType: String): String = withContext(Dispatchers.IO) {
        val key = ApiKeyManager.petsApiKey ?: error("Pets API key not loaded")
        NetworkModule
            .petService(animalType)
            .getRandomImage(apiKey = key)
            .firstOrNull()
            ?.url
            .orEmpty()
    }
}
