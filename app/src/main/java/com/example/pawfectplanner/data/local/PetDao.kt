package com.example.pawfectplanner.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.pawfectplanner.data.model.Pet
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets")
    fun getAllPets(): Flow<List<Pet>>

    @Insert
    suspend fun insertPet(pet: Pet): Long

    @Update
    suspend fun updatePet(pet: Pet)

    @Delete
    suspend fun deletePet(pet: Pet)
}
