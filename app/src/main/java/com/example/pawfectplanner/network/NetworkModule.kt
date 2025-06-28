package com.example.pawfectplanner.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {
    private val baseRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    val petService: PetApiService by lazy {
        baseRetrofit.create(PetApiService::class.java)
    }

    val serverService: ServerApiService by lazy {
        baseRetrofit
            .newBuilder()
            .baseUrl("https://YOUR_SERVER_URL/")
            .build()
            .create(ServerApiService::class.java)
    }

    val geminiService: GeminiApiService by lazy {
        baseRetrofit
            .newBuilder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .build()
            .create(GeminiApiService::class.java)
    }
}
