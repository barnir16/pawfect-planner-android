package com.example.pawfectplanner.network

import retrofit2.http.GET
import retrofit2.http.Header

interface BreedsDogApiService {
    @GET("breeds")
    suspend fun getAllBreeds(
        @Header("x-api-key") apiKey: String
    ): List<BreedInfoResponse>
} 