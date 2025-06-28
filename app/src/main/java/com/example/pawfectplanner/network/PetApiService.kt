package com.example.pawfectplanner.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

enum class PetType { DOG, CAT }

interface PetApiService {
    @GET("{type}/breeds/image/random")
    suspend fun getRandomBreedImage(
        @Path("type") type: String,
        @Query("api_key") apiKey: String
    ): PetImageResponse
}

data class PetImageResponse(
    val url: String
)
