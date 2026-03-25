package com.ai.adventchallenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.adventchallenge.api.ZaiApiService
import com.ai.adventchallenge.api.dtos.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatSettings(
    val systemPrompt: String = "",
    val temperature: Float = 0.7f,
    val maxCharacterCount: String = "",
    val maxTokens: String = "",
    val responseType: String = "text",
    val stopWord: String = ""
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val settings: ChatSettings = ChatSettings(),
    val error: String? = null
)

class ChatViewModel(
    val apiService: ZaiApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun updateSettings(settings: ChatSettings) {
        _uiState.value = _uiState.value.copy(settings = settings)
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val currentMessages = _uiState.value.messages.toMutableList()
            val settings = _uiState.value.settings

            val messagesToSend = mutableListOf<ChatMessage>()

            var systemPrompt = settings.systemPrompt
            if (settings.maxCharacterCount.isNotBlank()) {
                systemPrompt += "\nМаксимальная длина ответа: ${settings.maxCharacterCount} символов."
            }

            if (systemPrompt.isNotBlank()) {
                messagesToSend.add(
                    ChatMessage(
                        role = "system",
                        content = systemPrompt
                    )
                )
            }

            messagesToSend.addAll(currentMessages)
            messagesToSend.add(ChatMessage(role = "user", content = userMessage))

            currentMessages.add(ChatMessage(role = "user", content = userMessage))
            _uiState.value = _uiState.value.copy(messages = currentMessages)

            val maxTokens = settings.maxTokens.toIntOrNull()
            val responseType = if (settings.responseType == "json") "json_object" else "text"
            val stopWord = settings.stopWord.takeIf { it.isNotBlank() }

            val result = apiService.sendMessage(
                messages = messagesToSend,
                temperature = settings.temperature,
                maxTokens = maxTokens,
                responseType = responseType,
                stopWord = stopWord
            )

            result.fold(
                onSuccess = { (message, tokenCount) ->
                    val updatedMessages = currentMessages + message.copy(
                        characterCount = message.content.length,
                        tokenCount = tokenCount
                    )
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
