package com.example.pawfectplanner.ui.gemini

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pawfectplanner.data.repository.GeminiRepository

class GeminiAssistantViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeminiAssistantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GeminiAssistantViewModel(GeminiRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
