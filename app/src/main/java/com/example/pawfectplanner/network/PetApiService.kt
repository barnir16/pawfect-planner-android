package com.example.pawfectplanner.network

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

data class PetImageResponse(val url: String)

interface PetApiService {
    @GET("images/search")
    suspend fun getRandomImage(
        @Header("x-api-key") apiKey: String,
        @Query("limit") limit: Int = 1
    ): List<PetImageResponse>
}
