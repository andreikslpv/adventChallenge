package com.ai.adventchallenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.adventchallenge.domain.model.Agent
import com.ai.adventchallenge.domain.model.AgentSettings
import com.ai.adventchallenge.domain.model.AgentResponse
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.model.Session
import com.ai.adventchallenge.domain.repository.AgentRepository
import com.ai.adventchallenge.domain.repository.MessageRepository
import com.ai.adventchallenge.domain.repository.SessionRepository
import com.ai.adventchallenge.domain.usecase.ProcessAgentRequestUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class ChatUiState(
    val agents: List<Agent> = emptyList(),
    val selectedAgentId: String = "",
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val isSendingToAll: Boolean = false,
    val error: String? = null,
    val savedAgents: List<Agent> = emptyList(),
    val showAgentSelector: Boolean = false,
    val sessions: List<Session> = emptyList(),
    val selectedSessionId: String = ""
)

@OptIn(ExperimentalUuidApi::class)
class ChatViewModel(
    private val processAgentRequestUseCase: ProcessAgentRequestUseCase,
    private val agentRepository: AgentRepository,
    private val messageRepository: MessageRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ChatUiState(
            agents = emptyList(),
            selectedAgentId = "",
            savedAgents = emptyList(),
            showAgentSelector = false
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadSavedAgents()
        loadSessions()
    }

    private fun loadSavedAgents() {
        viewModelScope.launch {
            agentRepository.getAllAgents().collect { agents ->
                _uiState.value = _uiState.value.copy(savedAgents = agents)
            }
        }
    }

    private fun loadSessions() {
        viewModelScope.launch {
            sessionRepository.getAllSessions().collect { sessions ->
                _uiState.value = _uiState.value.copy(sessions = sessions)
                if (_uiState.value.selectedSessionId.isEmpty() && sessions.isNotEmpty()) {
                    selectSession(sessions.first().id)
                }
            }
        }
    }

    fun addAgent(agentId: String? = null) {
        if (agentId != null) {
            viewModelScope.launch {
                val agent = agentRepository.getAgentById(agentId)
                if (agent != null) {
                    _uiState.value = _uiState.value.copy(
                        agents = _uiState.value.agents + agent,
                        selectedAgentId = agent.id,
                        showAgentSelector = false
                    )
                    loadMessagesForAgent(agent.id)
                }
            }
        } else {
            val newAgent = Agent(id = Uuid.random().toString())
            _uiState.value = _uiState.value.copy(
                agents = _uiState.value.agents + newAgent,
                selectedAgentId = newAgent.id,
                showAgentSelector = false
            )
        }
    }

    fun removeAgent(agentId: String) {
        val newAgents = _uiState.value.agents.filterNot { it.id == agentId }
        val newSelectedId = if (_uiState.value.selectedAgentId == agentId) {
            newAgents.firstOrNull()?.id ?: ""
        } else {
            _uiState.value.selectedAgentId
        }
        _uiState.value = _uiState.value.copy(agents = newAgents, selectedAgentId = newSelectedId)
        
        viewModelScope.launch {
            agentRepository.deleteAgent(agentId)
        }
    }

    fun closeAgent(agentId: String) {
        val newAgents = _uiState.value.agents.filterNot { it.id == agentId }
        if (newAgents.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                agents = emptyList(),
                selectedAgentId = "",
                messages = emptyList()
            )
        } else if (_uiState.value.selectedAgentId == agentId) {
            _uiState.value = _uiState.value.copy(
                agents = newAgents,
                selectedAgentId = newAgents.first().id,
                messages = emptyList()
            )
        } else {
            _uiState.value = _uiState.value.copy(agents = newAgents)
        }
    }

    fun selectAgent(agentId: String) {
        _uiState.value = _uiState.value.copy(selectedAgentId = agentId)
        loadMessagesForAgent(agentId)
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

        viewModelScope.launch {
            val agent = updatedAgents.find { it.id == agentId }
            if (agent != null) {
                agentRepository.saveAgent(agent)
            }
        }
    }

    private fun loadMessagesForAgent(agentId: String) {
        viewModelScope.launch {
            val messages = messageRepository.getMessagesBySessionIdSync(_uiState.value.selectedSessionId)
            _uiState.value = _uiState.value.copy(messages = messages)
        }
    }

    fun clearSession() {
        val sessionId = _uiState.value.selectedSessionId
        _uiState.value = _uiState.value.copy(messages = emptyList())

        if (sessionId.isNotEmpty()) {
            viewModelScope.launch {
                messageRepository.deleteMessagesBySessionId(sessionId)
            }
        }
    }

    fun showAgentSelector() {
        _uiState.value = _uiState.value.copy(showAgentSelector = true)
    }

    fun hideAgentSelector() {
        _uiState.value = _uiState.value.copy(showAgentSelector = false)
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
            currentMessages.add(Message(
                id = Uuid.random().toString(),
                role = "user",
                content = userMessage
            ))
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
        val userMsg = Message(
            id = Uuid.random().toString(),
            role = "user",
            content = userMessage
        )
        currentMessages.add(userMsg)
        _uiState.value = _uiState.value.copy(messages = currentMessages, isLoading = true, error = null)

        viewModelScope.launch {
            val conversationHistory = currentMessages.dropLast(1)
            
            val response = processAgentRequestUseCase(agent, userMessage, conversationHistory)

            when (response) {
                is AgentResponse.Success -> {
                    val messageWithAgentInfo = response.message.copy(
                        id = response.message.id.ifEmpty { Uuid.random().toString() },
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

                    saveMessagesToDatabase(updatedMessages, agent.id)
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

    private suspend fun sendRequestToAgent(userMessage: String, agent: Agent, userMessages: List<Message>) {
        val response = processAgentRequestUseCase(agent, userMessage, userMessages)

        when (response) {
            is AgentResponse.Success -> {
                val messageWithAgentInfo = response.message.copy(
                    id = response.message.id.ifEmpty { Uuid.random().toString() },
                    systemPrompt = agent.settings.systemPrompt,
                    agentId = agent.id,
                    characterCount = response.message.content.length,
                    tokenCount = response.tokenCount
                )
                val currentMessages = _uiState.value.messages.toMutableList()
                currentMessages.add(messageWithAgentInfo)
                _uiState.value = _uiState.value.copy(messages = currentMessages)

                saveMessagesToDatabase(currentMessages, agent.id)
            }
            is AgentResponse.Error -> {
                _uiState.value = _uiState.value.copy(error = response.message)
            }
        }
    }

    private suspend fun saveMessagesToDatabase(messages: List<Message>, agentId: String) {
        val selectedAgent = _uiState.value.agents.find { it.id == agentId }
        if (selectedAgent != null) {
            agentRepository.saveAgent(selectedAgent)
        }
        messageRepository.saveMessages(messages, _uiState.value.selectedSessionId)
        
        val session = _uiState.value.sessions.find { it.id == _uiState.value.selectedSessionId }
        if (session != null && session.selectedAgentId != agentId) {
            val updatedSession = session.copy(selectedAgentId = agentId)
            sessionRepository.saveSession(updatedSession)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun createNewSession() {
        val sessionId = Uuid.random().toString()
        val newSession = Session(
            id = sessionId,
            name = "Новая сессия ${_uiState.value.sessions.size + 1}",
            selectedAgentId = _uiState.value.selectedAgentId
        )
        viewModelScope.launch {
            sessionRepository.saveSession(newSession)
            selectSession(sessionId)
        }
    }

    fun selectSession(sessionId: String) {
        _uiState.value = _uiState.value.copy(selectedSessionId = sessionId)
        viewModelScope.launch {
            val session = sessionRepository.getSessionById(sessionId)
            if (session != null) {
                _uiState.value = _uiState.value.copy(
                    selectedSessionId = sessionId,
                    selectedAgentId = if (session.selectedAgentId.isNotEmpty()) session.selectedAgentId else _uiState.value.selectedAgentId
                )
                loadMessagesForSession(sessionId)
                loadAgentsForSession(sessionId)
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            messageRepository.deleteMessagesBySessionId(sessionId)
            sessionRepository.deleteSession(sessionId)
            
            if (_uiState.value.selectedSessionId == sessionId) {
                val remainingSessions = _uiState.value.sessions.filterNot { it.id == sessionId }
                if (remainingSessions.isNotEmpty()) {
                    selectSession(remainingSessions.first().id)
                } else {
                    createNewSession()
                }
            }
        }
    }

    private fun loadMessagesForSession(sessionId: String) {
        viewModelScope.launch {
            val messages = messageRepository.getMessagesBySessionIdSync(sessionId)
            _uiState.value = _uiState.value.copy(messages = messages)
        }
    }

    private fun loadAgentsForSession(sessionId: String) {
        viewModelScope.launch {
            val messages = messageRepository.getMessagesBySessionIdSync(sessionId)
            val agentIds = messages.mapNotNull { it.agentId }.distinct()
            val agents = agentIds.mapNotNull { agentId ->
                _uiState.value.savedAgents.find { it.id == agentId }
            }.distinct()
            
            if (agents.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    agents = agents,
                    selectedAgentId = agents.first().id
                )
            }
        }
    }

    fun getSessionTokenCount(): Int {
        return _uiState.value.messages.sumOf { it.tokenCount }
    }

    fun getSelectedAgentMaxContextWindow(): Int {
        val selectedAgent = _uiState.value.agents.find { it.id == _uiState.value.selectedAgentId }
        return selectedAgent?.settings?.selectedModel?.maxContextWindow ?: 200000
    }
}
