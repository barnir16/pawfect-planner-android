package com.example.pawfectplanner.data.repository

import com.example.pawfectplanner.data.local.PetDao
import com.example.pawfectplanner.data.model.Pet
import kotlinx.coroutines.flow.Flow

class PetRepository(private val dao: PetDao) {
    val allPets: Flow<List<Pet>> = dao.getAllPets()

    suspend fun insert(pet: Pet): Long = dao.insertPet(pet)

    suspend fun update(pet: Pet) = dao.updatePet(pet)

    suspend fun delete(pet: Pet) = dao.deletePet(pet)
}
