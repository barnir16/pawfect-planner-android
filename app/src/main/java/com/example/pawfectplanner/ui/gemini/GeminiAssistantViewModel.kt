package com.example.pawfectplanner.ui.gemini

import androidx.lifecycle.*
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.data.repository.GeminiRepository
import com.example.pawfectplanner.data.repository.PetRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GeminiAssistantViewModel(
    private val geminiRepository: GeminiRepository,
    private val petRepository: PetRepository
) : ViewModel() {

    private val _chatMessages = MutableLiveData<List<Pair<String, Boolean>>>(emptyList())
    val chatMessages: LiveData<List<Pair<String, Boolean>>> = _chatMessages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun sendMessage(userInput: String) {
        _isLoading.value = true
        _error.value = null
        addMessage(userInput, isUser = true)

        viewModelScope.launch {
            try {
                val pets = petRepository.allPets.first()
                val petContext = buildPetContext(pets)
                val response = geminiRepository.sendMessage(userInput, petContext)
                addMessage(response, isUser = false)
            } catch (e: Exception) {
                _error.value = "Failed to process message."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun addMessage(message: String, isUser: Boolean) {
        val updated = _chatMessages.value.orEmpty() + Pair(message, isUser)
        _chatMessages.value = updated
    }

    private fun buildPetContext(pets: List<Pet>): String {
        if (pets.isEmpty()) return "The user has not added any pets yet."

        return pets.joinToString(separator = "\n\n") { pet ->
            buildString {
                append("Pet name: ${pet.name}. ")
                append("Breed: ${pet.breedType} - ${pet.breed}. ")
                if (pet.age > 0) append("Age: ${pet.age} years. ")
                pet.weightKg?.let { append("Weight: $it kg. ") }
                if (pet.healthIssues.isNotEmpty()) append("Health issues: ${pet.healthIssues.joinToString()}. ")
                if (pet.behaviorIssues.isNotEmpty()) append("Behavior issues: ${pet.behaviorIssues.joinToString()}. ")
            }.trim()
        }
    }
}
