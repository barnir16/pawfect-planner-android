package com.example.pawfectplanner.ui.gemini

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawfectplanner.data.repository.GeminiRepository
import kotlinx.coroutines.launch

class GeminiAssistantViewModel(
    private val repository: GeminiRepository
) : ViewModel() {

    private val _chatMessages = MutableLiveData<List<ChatMessage>>(emptyList())
    val chatMessages: LiveData<List<ChatMessage>> = _chatMessages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val messages = _chatMessages.value.orEmpty().toMutableList()
            messages.add(ChatMessage(text, true))
            _chatMessages.value = messages
            _isLoading.value = true
            try {
                val reply = repository.sendMessage(text)
                messages.add(ChatMessage(reply, false))
                _chatMessages.value = messages
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
