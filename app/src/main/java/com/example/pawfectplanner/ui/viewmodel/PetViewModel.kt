package com.example.pawfectplanner.ui.viewmodel

import androidx.lifecycle.*
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.data.repository.PetRepository
import kotlinx.coroutines.launch

class PetViewModel(private val repo: PetRepository) : ViewModel() {
    val allPets: LiveData<List<Pet>> = repo.allPets.asLiveData()

    fun insert(pet: Pet) = viewModelScope.launch { repo.insert(pet) }
    fun update(pet: Pet) = viewModelScope.launch { repo.update(pet) }
    fun delete(pet: Pet) = viewModelScope.launch { repo.delete(pet) }
}

class PetViewModelFactory(private val repo: PetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        if (modelClass.isAssignableFrom(PetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            PetViewModel(repo) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
}
