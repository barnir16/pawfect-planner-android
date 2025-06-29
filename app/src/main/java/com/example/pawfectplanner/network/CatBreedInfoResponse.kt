package com.example.pawfectplanner.network

data class CatBreedInfoResponse(
    val id: String,
    val name: String?,
    val life_span: String?,
    val temperament: String?,
    val weight: Weight?,
    val origin: String?
) 