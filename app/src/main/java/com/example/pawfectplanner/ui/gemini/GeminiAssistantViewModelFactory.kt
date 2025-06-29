package com.example.pawfectplanner.ui.gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pawfectplanner.PawfectPlannerApplication
import com.example.pawfectplanner.data.repository.GeminiRepository
import com.example.pawfectplanner.data.repository.PetRepository

class GeminiAssistantViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = PawfectPlannerApplication.instance
        val petRepository = PetRepository(app.database.petDao())
        val geminiRepository = GeminiRepository()
        return GeminiAssistantViewModel(geminiRepository, petRepository) as T
    }
}