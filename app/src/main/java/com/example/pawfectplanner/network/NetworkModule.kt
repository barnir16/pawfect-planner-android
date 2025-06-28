package com.example.pawfectplanner.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {

    fun petService(animalType: String): PetApiService =
        Retrofit.Builder()
            .baseUrl("https://api.the${animalType.lowercase()}api.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(PetApiService::class.java)

    private val geminiRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta/")
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                )
            )
            .build()
    }

    val geminiService: GeminiApiService by lazy {
        geminiRetrofit.create(GeminiApiService::class.java)
    }
}
