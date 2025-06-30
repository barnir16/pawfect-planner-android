package com.example.pawfectplanner.network

import com.example.pawfectplanner.util.Weight
import com.squareup.moshi.Json

data class BreedInfoResponse(
    val id: Int,
    val name: String,
    @Json(name = "life_span")
    val lifeSpan: String?,
    val temperament: String?,
    val weight: Weight?,
    @Json(name = "bred_for")
    val bredFor: String?,
    @Json(name = "breed_group")
    val breedGroup: String?
)