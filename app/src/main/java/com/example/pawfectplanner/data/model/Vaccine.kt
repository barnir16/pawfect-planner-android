package com.example.pawfectplanner.data.model

import com.google.gson.annotations.SerializedName

data class Vaccine(
    val name: String,
    val frequency: String,
    @SerializedName("first_dose_age")
    val firstDoseAge: String? = null,
    @SerializedName("kitten_schedule")
    val kittenSchedule: List<String>? = null,
    @SerializedName("puppy_schedule")
    val puppySchedule: List<String>? = null,
    val description: String,
    @SerializedName("side_effects")
    val sideEffects: List<String>? = null,
    @SerializedName("age_restriction")
    val ageRestriction: AgeRestriction? = null,
    @SerializedName("last_updated")
    val lastUpdated: String,
    @SerializedName("common_treatments")
    val commonTreatments: List<String>? = null
)

data class AgeRestriction(
    @SerializedName("min_weeks")
    val minWeeks: Int?,
    @SerializedName("max_years")
    val maxYears: Int?
) 