package com.example.pawfectplanner.ui.gemini

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pawfectplanner.data.repository.GeminiRepository

class GeminiAssistantViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GeminiAssistantViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GeminiAssistantViewModel(GeminiRepository(context)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
