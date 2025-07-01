package com.example.pawfectplanner.ui.gemini

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawfectplanner.data.model.Pet
import com.example.pawfectplanner.data.repository.GeminiRepository
import com.example.pawfectplanner.data.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GeminiAssistantViewModel @Inject constructor(
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
                val context = buildPetContext(pets)
                val response = geminiRepository.sendMessage(userInput, context)
                addMessage(response, isUser = false)
            } catch (e: Exception) {
                _error.value = "Failed to process message."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun addMessage(message: String, isUser: Boolean) {
        val updated = _chatMessages.value.orEmpty() + (message to isUser)
        _chatMessages.value = updated
    }

    private fun buildPetContext(pets: List<Pet>): String {
        if (pets.isEmpty()) return "The user has not added any pets yet."

        return pets.joinToString("\n\n") { pet ->
            buildString {
                append("Pet name: ${pet.name}. ")
                append("Breed: ${pet.breedType} - ${pet.breed}. ")
                pet.age?.takeIf { it > 0 }?.let { append("Age: $it years. ") }
                pet.weightKg?.let { append("Weight: $it kg. ") }
                if (pet.healthIssues.isNotEmpty()) {
                    append("Health issues: ${pet.healthIssues.joinToString()}. ")
                }
                if (pet.behaviorIssues.isNotEmpty()) {
                    append("Behavior issues: ${pet.behaviorIssues.joinToString()}. ")
                }
            }.trim()
        }
    }
}
