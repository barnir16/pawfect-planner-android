package com.example.pawfectplanner.ui.gemini

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GeminiAssistantViewModel : ViewModel() {
    private val _chatMessages = MutableLiveData<List<ChatMessage>>(emptyList())
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun sendMessage(message: String) {
        viewModelScope.launch {
            val current = _chatMessages.value!!.toMutableList()
            current.add(ChatMessage(text = message, isUser = true))
            _chatMessages.value = current

            _isLoading.value = true

            try {
                delay(1000)
                val response = "Please try again"
                current.add(ChatMessage(text = response, isUser = false))
                _chatMessages.value = current
            } finally {
                _isLoading.value = false
            }
        }
    }
}
