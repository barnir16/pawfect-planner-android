package com.example.pawfectplanner.ui.gemini

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class GeminiAssistantViewModel : ViewModel() {
    private val _chatMessages = MutableLiveData<List<ChatMessage>>(emptyList())
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun sendMessage(message: String) {
        viewModelScope.launch {
            val currentMessages = _chatMessages.value?.toMutableList() ?: mutableListOf()
            currentMessages.add(ChatMessage(message, true))
            _chatMessages.value = currentMessages

            _isLoading.value = true

            try {
                // TODO: Gotta actually implement gemini here
                kotlinx.coroutines.delay(1000)
                val response = "אמרתי שזה לא עובד מה אתם מנסים סתם?"

                currentMessages.add(ChatMessage(response, false))
                _chatMessages.value = currentMessages
            } finally {
                _isLoading.value = false
            }
        }
    }
} 