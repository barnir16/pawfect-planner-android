package com.example.pawfectplanner.network

import com.example.pawfectplanner.util.Weight
import com.squareup.moshi.Json

data class CatBreedInfoResponse(
    val id: String,
    val name: String?,
    @Json(name = "life_span")
    val lifeSpan: String?,
    val temperament: String?,
    val weight: Weight?,
    val origin: String?
)