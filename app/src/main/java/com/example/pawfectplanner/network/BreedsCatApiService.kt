package com.example.pawfectplanner.network

import retrofit2.http.GET
import retrofit2.http.Header

interface BreedsCatApiService {
    @GET("breeds")
    suspend fun getAllCatBreeds(
        @Header("x-api-key") apiKey: String
    ): List<CatBreedInfoResponse>
} 