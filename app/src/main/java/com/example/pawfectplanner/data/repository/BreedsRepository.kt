package com.example.pawfectplanner.data.repository

import com.example.pawfectplanner.network.*

class BreedsRepository(
    private val dogApiService: BreedsDogApiService,
    private val catApiService: BreedsCatApiService
) {

    suspend fun getDogBreedInfo(breedName: String, apiKey: String): Result<BreedInfoResponse> {
        return try {
            val allBreeds = dogApiService.getAllBreeds(apiKey)
            val breed = allBreeds.firstOrNull { it.name.equals(breedName, ignoreCase = true) }
                ?: return Result.failure(Exception("Dog breed not found"))
            Result.success(breed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllDogBreeds(apiKey: String): Result<List<BreedInfoResponse>> {
        return try {
            val breeds = dogApiService.getAllBreeds(apiKey)
            Result.success(breeds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCatBreedInfo(breedName: String, apiKey: String): Result<CatBreedInfoResponse> {
        return try {
            val allBreeds = catApiService.getAllCatBreeds(apiKey)
            val breed = allBreeds.firstOrNull { it.name.equals(breedName, ignoreCase = true) }
                ?: return Result.failure(Exception("Cat breed not found"))
            Result.success(breed)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllCatBreeds(apiKey: String): Result<List<CatBreedInfoResponse>> {
        return try {
            val breeds = catApiService.getAllCatBreeds(apiKey)
            Result.success(breeds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 