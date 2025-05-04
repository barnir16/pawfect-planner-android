package com.example.pawfectplanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val breedType: String,
    val breed: String,
    val birthDate: LocalDate,
    val age: Int,
    val weightKg: Double? = null,
    val photoUri: String? = null,
    val healthIssues: List<String> = emptyList(),
    val behaviorIssues: List<String> = emptyList()
)
