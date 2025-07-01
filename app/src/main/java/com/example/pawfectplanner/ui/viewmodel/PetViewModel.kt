package com.example.pawfectplanner.ui.viewmodel

import androidx.lifecycle.*
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.data.repository.BreedsRepository
import com.example.pawfectplanner.data.repository.PetRepository
import com.example.pawfectplanner.network.BreedInfoResponse
import com.example.pawfectplanner.network.CatBreedInfoResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PetViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val breedsRepository: BreedsRepository
) : ViewModel() {

    val allPets: LiveData<List<Pet>> = petRepository.allPets.asLiveData()

    private val _dogBreed = MutableStateFlow<BreedInfoResponse?>(null)
    val dogBreed: StateFlow<BreedInfoResponse?> = _dogBreed

    private val _catBreed = MutableStateFlow<CatBreedInfoResponse?>(null)
    val catBreed: StateFlow<CatBreedInfoResponse?> = _catBreed

    private val _breedList = MutableStateFlow<List<String>>(emptyList())
    val breedList: StateFlow<List<String>> = _breedList

    fun insert(pet: Pet) = viewModelScope.launch { petRepository.insert(pet) }
    fun update(pet: Pet) = viewModelScope.launch { petRepository.update(pet) }
    fun delete(pet: Pet) = viewModelScope.launch { petRepository.delete(pet) }

    fun fetchDogBreed(breedName: String, apiKey: String) {
        viewModelScope.launch {
            val result = breedsRepository.getDogBreedInfo(breedName, apiKey)
            _dogBreed.value = result.getOrNull()
        }
    }

    fun fetchCatBreed(breedName: String, apiKey: String) {
        viewModelScope.launch {
            val result = breedsRepository.getCatBreedInfo(breedName, apiKey)
            _catBreed.value = result.getOrNull()
        }
    }

    fun fetchBreeds(breedType: String, apiKeyDog: String, apiKeyCat: String) {
        viewModelScope.launch {
            val breeds = try {
                if (breedType.equals("Dog", ignoreCase = true)) {
                    val result = breedsRepository.getAllDogBreeds(apiKeyDog)
                    val breedNames = result.getOrNull()?.mapNotNull { it.name } ?: emptyList()
                    breedNames
                } else if (breedType.equals("Cat", ignoreCase = true)) {
                    val result = breedsRepository.getAllCatBreeds(apiKeyCat)
                    val breedNames = result.getOrNull()?.mapNotNull { it.name } ?: emptyList()
                    breedNames
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
            _breedList.value = breeds
        }
    }
}


