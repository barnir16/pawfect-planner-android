package com.example.pawfectplanner.network

import retrofit2.http.GET

interface ServerApiService {
    @GET("keys")
    suspend fun fetchKeys(): ApiKeys
}
