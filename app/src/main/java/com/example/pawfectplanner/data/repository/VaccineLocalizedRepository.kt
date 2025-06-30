package com.example.pawfectplanner.data.repository

import android.content.Context
import com.example.pawfectplanner.R
import com.example.pawfectplanner.data.model.Vaccine

class VaccineLocalizedRepository(private val context: Context) {
    
    fun getCatVaccines(): List<Vaccine> {
        return listOf(
            Vaccine(
                name = context.getString(R.string.vaccine_rabies_cat),
                frequency = context.getString(R.string.vaccine_freq_yearly),
                firstDoseAge = context.getString(R.string.vaccine_age_4_months),
                description = context.getString(R.string.vaccine_desc_rabies_cat),
                sideEffects = listOf(
                    context.getString(R.string.vaccine_side_lethargy),
                    context.getString(R.string.vaccine_side_mild_fever)
                ),
                lastUpdated = "2025-01-01"
            ),
            Vaccine(
                name = context.getString(R.string.vaccine_quadrivalent),
                frequency = context.getString(R.string.vaccine_freq_yearly),
                kittenSchedule = listOf(
                    context.getString(R.string.vaccine_age_8_weeks),
                    context.getString(R.string.vaccine_age_12_weeks)
                ),
                description = context.getString(R.string.vaccine_desc_quadrivalent),
                sideEffects = listOf(context.getString(R.string.vaccine_side_injection_swelling)),
                lastUpdated = "2025-01-01"
            )
        )
    }
    
    fun getDogVaccines(): List<Vaccine> {
        return listOf(
            Vaccine(
                name = context.getString(R.string.vaccine_rabies_dog),
                frequency = context.getString(R.string.vaccine_freq_2_years),
                firstDoseAge = context.getString(R.string.vaccine_age_3_months),
                description = context.getString(R.string.vaccine_desc_rabies_dog),
                sideEffects = listOf(
                    context.getString(R.string.vaccine_side_lethargy),
                    context.getString(R.string.vaccine_side_fever)
                ),
                lastUpdated = "2025-01-01"
            ),
            Vaccine(
                name = context.getString(R.string.vaccine_hexavalent),
                frequency = context.getString(R.string.vaccine_freq_yearly),
                puppySchedule = listOf(
                    context.getString(R.string.vaccine_age_6_weeks),
                    context.getString(R.string.vaccine_age_9_weeks),
                    context.getString(R.string.vaccine_age_12_weeks)
                ),
                description = context.getString(R.string.vaccine_desc_hexavalent),
                sideEffects = listOf(context.getString(R.string.vaccine_side_mild_fever)),
                lastUpdated = "2025-01-01"
            ),
            Vaccine(
                name = context.getString(R.string.vaccine_parkworm),
                frequency = context.getString(R.string.vaccine_freq_2_months),
                description = context.getString(R.string.vaccine_desc_parkworm),
                sideEffects = listOf(
                    context.getString(R.string.vaccine_side_diarrhea),
                    context.getString(R.string.vaccine_side_nausea)
                ),
                lastUpdated = "2025-01-01"
            ),
            Vaccine(
                name = context.getString(R.string.vaccine_kennel_cough),
                frequency = context.getString(R.string.vaccine_freq_6_months),
                description = context.getString(R.string.vaccine_desc_kennel_cough),
                sideEffects = listOf(context.getString(R.string.vaccine_side_sneezing)),
                lastUpdated = "2025-01-01"
            )
        )
    }
    
    fun getAllVaccines(): List<Vaccine> {
        return getCatVaccines() + getDogVaccines()
    }
    
    fun getVaccinesByPetType(petType: String): List<Vaccine> {
        return when (petType.lowercase()) {
            "cat", "kitten" -> getCatVaccines()
            "dog", "puppy" -> getDogVaccines()
            else -> emptyList()
        }
    }
    
    fun searchVaccines(query: String): List<Vaccine> {
        val allVaccines = getAllVaccines()
        return allVaccines.filter { vaccine ->
            vaccine.name.contains(query, ignoreCase = true) ||
            vaccine.description.contains(query, ignoreCase = true)
        }
    }
} 