package com.ai.adventchallenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.adventchallenge.api.ZaiApiService
import com.ai.adventchallenge.api.dtos.ChatMessage
import com.ai.adventchallenge.agent.Agent
import com.ai.adventchallenge.agent.AgentResponse
import com.ai.adventchallenge.agent.AgentSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class ChatUiState(
    val agents: List<Agent> = listOf(Agent(id = Uuid.random().toString())),
    val selectedAgentId: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSendingToAll: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalUuidApi::class)
class ChatViewModel(
    val apiService: ZaiApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ChatUiState(
            agents = listOf(Agent(id = Uuid.random().toString())),
            selectedAgentId = ""
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        _uiState.value = _uiState.value.copy(selectedAgentId = _uiState.value.agents.first().id)
    }

    fun addAgent() {
        val newAgent = Agent(id = Uuid.random().toString())
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

    fun updateAgentSettings(agentId: String, settings: AgentSettings) {
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
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(ChatMessage(role = "user", content = userMessage))
            _uiState.value = _uiState.value.copy(messages = currentMessages, isSendingToAll = true, error = null)

            val userMessages = currentMessages.filter { it.role == "user" }
            
            _uiState.value.agents.forEach { agent ->
                sendRequestToAgent(userMessage, agent, userMessages)
            }

            _uiState.value = _uiState.value.copy(isSendingToAll = false)
        }
    }

    private fun sendMessageToAgent(userMessage: String, agent: Agent) {
        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(ChatMessage(role = "user", content = userMessage))
        _uiState.value = _uiState.value.copy(messages = currentMessages, isLoading = true, error = null)

        viewModelScope.launch {
            val conversationHistory = currentMessages.dropLast(1)
            
            val response = agent.processRequest(userMessage, conversationHistory, apiService)

            when (response) {
                is AgentResponse.Success -> {
                    val messageWithAgentInfo = response.message.copy(
                        systemPrompt = agent.settings.systemPrompt,
                        agentId = agent.id,
                        characterCount = response.message.content.length,
                        tokenCount = response.tokenCount
                    )
                    val updatedMessages = _uiState.value.messages + messageWithAgentInfo
                    _uiState.value = _uiState.value.copy(
                        messages = updatedMessages,
                        isLoading = false
                    )
                }
                is AgentResponse.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = response.message
                    )
                }
            }
        }
    }

    private suspend fun sendRequestToAgent(userMessage: String, agent: Agent, userMessages: List<ChatMessage>) {
        val response = agent.processRequest(userMessage, userMessages, apiService)

        when (response) {
            is AgentResponse.Success -> {
                val messageWithAgentInfo = response.message.copy(
                    systemPrompt = agent.settings.systemPrompt,
                    agentId = agent.id,
                    characterCount = response.message.content.length,
                    tokenCount = response.tokenCount
                )
                val currentMessages = _uiState.value.messages.toMutableList()
                currentMessages.add(messageWithAgentInfo)
                _uiState.value = _uiState.value.copy(messages = currentMessages)
            }
            is AgentResponse.Error -> {
                _uiState.value = _uiState.value.copy(error = response.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
