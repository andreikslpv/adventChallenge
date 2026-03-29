package com.ai.adventchallenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.adventchallenge.api.ZaiApiService
import com.ai.adventchallenge.api.dtos.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ChatSettings(
    val systemPrompt: String = "",
    val temperature: Float = 0.7f,
    val maxCharacterCount: String = "",
    val maxTokens: String = "",
    val responseType: String = "text",
    val stopWord: String = ""
)

data class Agent(
    val id: String,
    val settings: ChatSettings = ChatSettings()
) {
    companion object {
        @OptIn(ExperimentalUuidApi::class)
        fun create() = Agent(
            id = Uuid.random().toString()
        )
    }
}

data class ChatUiState(
    val agents: List<Agent> = listOf(Agent.create()),
    val selectedAgentId: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSendingToAll: Boolean = false,
    val error: String? = null
)

class ChatViewModel(
    val apiService: ZaiApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ChatUiState(
            agents = listOf(Agent.create()),
            selectedAgentId = ""
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(selectedAgentId = _uiState.value.agents.first().id)
    }

    fun addAgent() {
        val newAgent = Agent.create()
        _uiState.value = _uiState.value.copy(agents = _uiState.value.agents + newAgent)
    }

    fun removeAgent(agentId: String) {
        if (_uiState.value.agents.size <= 1) return
        val newAgents = _uiState.value.agents.filterNot { it.id == agentId }
        val newSelectedId = if (_uiState.value.selectedAgentId == agentId) {
            newAgents.first().id
        } else {
            _uiState.value.selectedAgentId
        }
        _uiState.value = _uiState.value.copy(agents = newAgents, selectedAgentId = newSelectedId)
    }

    fun selectAgent(agentId: String) {
        _uiState.value = _uiState.value.copy(selectedAgentId = agentId)
    }

    fun updateAgentSettings(agentId: String, settings: ChatSettings) {
        val updatedAgents = _uiState.value.agents.map { agent ->
            if (agent.id == agentId) {
                agent.copy(settings = settings)
            } else {
                agent
            }
        }
        _uiState.value = _uiState.value.copy(agents = updatedAgents)
    }

    fun clearSession() {
        _uiState.value = _uiState.value.copy(messages = emptyList())
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        val selectedAgent = _uiState.value.agents.find { it.id == _uiState.value.selectedAgentId } ?: return
        sendMessageToAgent(userMessage, selectedAgent)
    }

    fun sendMessageToAll(userMessage: String) {
        if (userMessage.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSendingToAll = true, error = null)

            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(ChatMessage(role = "user", content = userMessage))
            _uiState.value = _uiState.value.copy(messages = currentMessages)

            _uiState.value.agents.forEach { agent ->
                viewModelScope.launch {
                    sendRequestToAgent(userMessage, agent, currentMessages.filter { it.role == "user" })
                }
            }

            _uiState.value = _uiState.value.copy(isSendingToAll = false)
        }
    }

    private fun sendMessageToAgent(userMessage: String, agent: Agent) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val currentMessages = _uiState.value.messages.toMutableList()
            val settings = agent.settings

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
                    val messageWithAgentInfo = message.copy(
                        systemPrompt = settings.systemPrompt,
                        agentId = agent.id,
                        characterCount = message.content.length,
                        tokenCount = tokenCount
                    )
                    val updatedMessages = currentMessages + messageWithAgentInfo
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

    private suspend fun sendRequestToAgent(userMessage: String, agent: Agent, userMessages: List<ChatMessage>) {
        val settings = agent.settings
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

        messagesToSend.addAll(userMessages)
        messagesToSend.add(ChatMessage(role = "user", content = userMessage))

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
                val messageWithAgentInfo = message.copy(
                    systemPrompt = settings.systemPrompt,
                    agentId = agent.id,
                    characterCount = message.content.length,
                    tokenCount = tokenCount
                )
                val currentMessages = _uiState.value.messages.toMutableList()
                currentMessages.add(messageWithAgentInfo)
                _uiState.value = _uiState.value.copy(messages = currentMessages)
            },
            onFailure = { exception ->
                _uiState.value = _uiState.value.copy(error = exception.message)
            }
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
