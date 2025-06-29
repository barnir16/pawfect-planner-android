package com.example.pawfectplanner.network

data class BreedInfoResponse(
    val id: Int,
    val name: String,
    val life_span: String?,
    val temperament: String?,
    val weight: Weight?,
    val bred_for: String?,
    val breed_group: String?
) 