package com.example.pawfectplanner.data.repository

import com.example.pawfectplanner.data.local.PetDao
import com.example.pawfectplanner.data.model.Pet
import kotlinx.coroutines.flow.Flow

class PetRepository(private val dao: PetDao) {
    val allPets: Flow<List<Pet>> = dao.getAllPets()

    suspend fun insert(pet: Pet): Long = dao.insert(pet)

    suspend fun update(pet: Pet) = dao.update(pet)

    suspend fun delete(pet: Pet) = dao.delete(pet)
}
