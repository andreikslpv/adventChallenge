package com.ai.adventchallenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.adventchallenge.api.ZaiApiService
import com.ai.adventchallenge.api.dtos.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val systemPrompt: String = "",
    val temperature: Float = 0.7f,
    val error: String? = null
)

class ChatViewModel(
    val apiService: ZaiApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun setSystemPrompt(prompt: String, temperature: Float = 0.7f) {
        _uiState.value = _uiState.value.copy(systemPrompt = prompt, temperature = temperature)
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val currentMessages = _uiState.value.messages.toMutableList()

            val messagesToSend = mutableListOf<ChatMessage>()

            if (_uiState.value.systemPrompt.isNotBlank()) {
                messagesToSend.add(
                    ChatMessage(
                        role = "system",
                        content = _uiState.value.systemPrompt
                    )
                )
            }

            messagesToSend.addAll(currentMessages)
            messagesToSend.add(ChatMessage(role = "user", content = userMessage))

            currentMessages.add(ChatMessage(role = "user", content = userMessage))
            _uiState.value = _uiState.value.copy(messages = currentMessages)

            val result = apiService.sendMessage(messagesToSend, _uiState.value.temperature)

            result.fold(
                onSuccess = { response ->
                    val updatedMessages = currentMessages + response
                    _uiState.value = _uiState.value.copy(
                        messages = updatedMessages,
                        isLoading = false
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
