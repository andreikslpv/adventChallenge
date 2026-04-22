package com.ai.adventchallenge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.adventchallenge.domain.context.ContextSettings
import com.ai.adventchallenge.domain.context.ContextStrategy
import com.ai.adventchallenge.domain.context.ContextStrategyFactory
import com.ai.adventchallenge.domain.model.Agent
import com.ai.adventchallenge.domain.model.AgentResponse
import com.ai.adventchallenge.domain.model.AgentSettings
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.model.Session
import com.ai.adventchallenge.domain.repository.AgentRepository
import com.ai.adventchallenge.domain.repository.MessageRepository
import com.ai.adventchallenge.domain.repository.SessionRepository
import com.ai.adventchallenge.domain.service.MainAgent
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
    private val mainAgent: MainAgent,
    private val agentRepository: AgentRepository,
    private val messageRepository: MessageRepository,
    private val sessionRepository: SessionRepository,
    private val strategyFactory: ContextStrategyFactory
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

    private var currentStrategy: ContextStrategy? = null

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
                if (_uiState.value.selectedSessionId.isEmpty()) {
                    if (sessions.isNotEmpty()) {
                        selectSession(sessions.first().id)
                    } else {
                        createNewSession()
                    }
                } else {
                    val currentSession = sessions.find { it.id == _uiState.value.selectedSessionId }
                    if (currentSession != null) {
                        updateStrategy(currentSession.contextSettings)
                    }
                }
            }
        }
    }

    private fun updateStrategy(settings: ContextSettings) {
        currentStrategy = strategyFactory.create(settings)
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
                    loadMessagesForSession(_uiState.value.selectedSessionId)
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

    private suspend fun updateSessionNameIfNeeded(firstMessage: String) {
        val sessionId = _uiState.value.selectedSessionId
        val session = _uiState.value.sessions.find { it.id == sessionId }

        if (session != null && session.name.startsWith("Новая сессия")) {
            val newName =
                firstMessage.take(50).let { if (firstMessage.length > 50) "$it..." else it }
            val updatedSession = session.copy(name = newName)
            sessionRepository.saveSession(updatedSession)
            _uiState.value = _uiState.value.copy(
                sessions = _uiState.value.sessions.map { if (it.id == sessionId) updatedSession else it }
            )
        }
    }

    fun clearSession() {
        val sessionId = _uiState.value.selectedSessionId
        _uiState.value = _uiState.value.copy(messages = emptyList())

        if (sessionId.isNotEmpty()) {
            viewModelScope.launch {
                messageRepository.deleteMessagesBySessionId(sessionId)
                sessionRepository.updateSessionSummary(sessionId, "")
                currentStrategy?.reset()
            }
        }
    }

    fun showAgentSelector() {
        _uiState.value = _uiState.value.copy(showAgentSelector = true)
    }

    fun updateSessionContextSettings(settings: ContextSettings) {
        val sessionId = _uiState.value.selectedSessionId
        if (sessionId.isNotEmpty()) {
            viewModelScope.launch {
                sessionRepository.updateSessionContextSettings(sessionId, settings)
                updateStrategy(settings)
            }
        }
    }

    fun hideAgentSelector() {
        _uiState.value = _uiState.value.copy(showAgentSelector = false)
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        val selectedAgent =
            _uiState.value.agents.find { it.id == _uiState.value.selectedAgentId } ?: return
        sendMessageToAgent(userMessage, selectedAgent)
    }

    fun sendMessageToAll(userMessage: String) {
        if (userMessage.isBlank()) return
        viewModelScope.launch {
            val outgoingTokens = userMessage.length / 4
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(
                Message(
                    id = Uuid.random().toString(),
                    role = "user",
                    content = userMessage,
                    characterCount = userMessage.length,
                    outgoingTokenCount = outgoingTokens
                )
            )
            _uiState.value =
                _uiState.value.copy(messages = currentMessages, isSendingToAll = true, error = null)

            val userMessages = currentMessages.filter { it.role == "user" }

            _uiState.value.agents.forEach { agent ->
                sendRequestToAgent(userMessage, agent, userMessages)
            }

            _uiState.value = _uiState.value.copy(isSendingToAll = false)
        }
    }

    private fun sendMessageToAgent(userMessage: String, agent: Agent) {
        val currentMessages = _uiState.value.messages.toMutableList()
        val outgoingTokens = userMessage.length / 4
        val userMsg = Message(
            id = Uuid.random().toString(),
            role = "user",
            content = userMessage,
            characterCount = userMessage.length,
            outgoingTokenCount = outgoingTokens,
            isSummarized = false
        )
        currentMessages.add(userMsg)
        _uiState.value =
            _uiState.value.copy(messages = currentMessages, isLoading = true, error = null)

        viewModelScope.launch {
            val sessionId = _uiState.value.selectedSessionId

            messageRepository.saveMessage(userMsg, sessionId)

            currentStrategy?.onUserMessage(userMsg)

            val session = sessionRepository.getSessionById(sessionId)
            val allMessages = messageRepository.getMessagesBySessionIdSync(sessionId)

            val contextMessages = currentStrategy?.buildContext(allMessages) ?: allMessages

            val response = mainAgent.processRequest(agent, userMessage, contextMessages, "")

            when (response) {
                is AgentResponse.Success -> {
                    val messageWithAgentInfo = response.message.copy(
                        id = response.message.id.ifEmpty { Uuid.random().toString() },
                        systemPrompt = agent.settings.systemPrompt,
                        agentId = agent.id,
                        characterCount = response.message.content.length,
                        tokenCount = response.tokenCount,
                        isSummarized = false
                    )
                    val updatedMessages = _uiState.value.messages + messageWithAgentInfo
                    _uiState.value = _uiState.value.copy(
                        messages = updatedMessages,
                        isLoading = false
                    )

                    updateSessionNameIfNeeded(userMessage)
                    messageRepository.saveMessage(messageWithAgentInfo, sessionId)

                    currentStrategy?.onAssistantMessage(messageWithAgentInfo)

                    val selectedAgent = _uiState.value.agents.find { it.id == agent.id }
                    if (selectedAgent != null) {
                        agentRepository.saveAgent(selectedAgent)
                    }

                    if (session != null && session.selectedAgentId != agent.id) {
                        val newSession = session.copy(selectedAgentId = agent.id)
                        sessionRepository.saveSession(newSession)
                    }
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

    private suspend fun sendRequestToAgent(
        userMessage: String,
        agent: Agent,
        userMessages: List<Message>
    ) {
        val sessionId = _uiState.value.selectedSessionId
        val session = sessionRepository.getSessionById(sessionId)
        val allMessages = messageRepository.getMessagesBySessionIdSync(sessionId)

        val contextMessages = currentStrategy?.buildContext(allMessages) ?: allMessages

        when (val response = mainAgent.processRequest(agent, userMessage, contextMessages, "")) {
            is AgentResponse.Success -> {
                val messageWithAgentInfo = response.message.copy(
                    id = response.message.id.ifEmpty { Uuid.random().toString() },
                    systemPrompt = agent.settings.systemPrompt,
                    agentId = agent.id,
                    characterCount = response.message.content.length,
                    tokenCount = response.tokenCount,
                    isSummarized = false
                )
                val currentMessages = _uiState.value.messages.toMutableList()
                currentMessages.add(messageWithAgentInfo)
                _uiState.value = _uiState.value.copy(messages = currentMessages)

                updateSessionNameIfNeeded(userMessage)
                messageRepository.saveMessage(messageWithAgentInfo, sessionId)

                currentStrategy?.onAssistantMessage(messageWithAgentInfo)

                val selectedAgent = _uiState.value.agents.find { it.id == agent.id }
                if (selectedAgent != null) {
                    agentRepository.saveAgent(selectedAgent)
                }

                if (session != null && session.selectedAgentId != agent.id) {
                    val newSession = session.copy(selectedAgentId = agent.id)
                    sessionRepository.saveSession(newSession)
                }
            }

            is AgentResponse.Error -> {
                _uiState.value = _uiState.value.copy(error = response.message)
            }
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
                updateStrategy(session.contextSettings)
                _uiState.value = _uiState.value.copy(
                    selectedSessionId = sessionId,
                    selectedAgentId = session.selectedAgentId.ifEmpty { _uiState.value.selectedAgentId }
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
            val agentIds = messages.map { it.agentId }.distinct()
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

    fun getCurrentSession(): Session? {
        return _uiState.value.sessions.find { it.id == _uiState.value.selectedSessionId }
    }

    fun getSessionTokenCount(): Int {
        return _uiState.value.messages.sumOf { it.tokenCount + it.outgoingTokenCount }
    }

    fun getSelectedAgentMaxContextWindow(): Int {
        val selectedAgent = _uiState.value.agents.find { it.id == _uiState.value.selectedAgentId }
        return selectedAgent?.settings?.selectedModel?.maxContextWindow ?: 200000
    }
}
