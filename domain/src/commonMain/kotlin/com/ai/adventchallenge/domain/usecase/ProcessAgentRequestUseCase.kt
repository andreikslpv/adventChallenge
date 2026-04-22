package com.ai.adventchallenge.domain.usecase

import com.ai.adventchallenge.domain.model.Agent
import com.ai.adventchallenge.domain.model.AgentResponse
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.model.Role
import com.ai.adventchallenge.domain.service.AIService

class ProcessAgentRequestUseCase(
    private val aiService: AIService
) {
    suspend operator fun invoke(
        agent: Agent,
        userMessage: String,
        conversationHistory: List<Message> = emptyList()
    ): AgentResponse {
        return try {
            val messagesToSend = buildMessageList(
                agent.settings.systemPrompt,
                userMessage,
                conversationHistory
            )

            val maxTokens = agent.settings.maxTokens.toIntOrNull()
            val responseType = if (agent.settings.responseType == "json") "json_object" else "text"
            val stopWord = agent.settings.stopWord.takeIf { it.isNotBlank() }

            val result = aiService.sendMessage(
                messages = messagesToSend,
                temperature = agent.settings.temperature,
                maxTokens = maxTokens,
                responseType = responseType,
                stopWord = stopWord,
                model = agent.settings.selectedModel
            )

            result.fold(
                onSuccess = { (message, tokenCount) ->
                    AgentResponse.Success(message, tokenCount)
                },
                onFailure = { exception ->
                    AgentResponse.Error(exception.message ?: "Unknown error")
                }
            )
        } catch (e: Exception) {
            AgentResponse.Error("Processing failed: ${e.message}")
        }
    }

    private fun buildMessageList(
        systemPrompt: String,
        userMessage: String,
        conversationHistory: List<Message>
    ): List<Message> {
        val messages = mutableListOf<Message>()

        if (systemPrompt.isNotBlank()) {
            messages.add(
                Message(
                    role = Role.SYSTEM,
                    content = systemPrompt
                )
            )
        }

        messages.addAll(conversationHistory)
        messages.add(Message(role = Role.USER, content = userMessage))

        return messages
    }
}
