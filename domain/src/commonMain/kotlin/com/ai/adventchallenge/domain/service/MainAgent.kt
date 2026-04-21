package com.ai.adventchallenge.domain.service

import com.ai.adventchallenge.domain.model.Agent
import com.ai.adventchallenge.domain.model.AgentResponse
import com.ai.adventchallenge.domain.model.Message

class MainAgent(
    private val aiService: AIService
) {
    suspend fun processRequest(
        agent: Agent,
        userMessage: String,
        conversationHistory: List<Message>,
        sessionSummary: String
    ): AgentResponse {
        return try {
            val messagesToSend = buildMessageList(
                agent.settings.systemPrompt,
                sessionSummary,
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
        sessionSummary: String,
        userMessage: String,
        conversationHistory: List<Message>
    ): List<Message> {
        val messages = mutableListOf<Message>()

        if (systemPrompt.isNotBlank()) {
            messages.add(
                Message(
                    role = "system",
                    content = systemPrompt
                )
            )
        }

        if (sessionSummary.isNotBlank()) {
            messages.add(
                Message(
                    role = "system",
                    content = "Conversation summary:\n\n$sessionSummary"
                )
            )
        }

        messages.addAll(conversationHistory)
        messages.add(Message(role = "user", content = userMessage))

        return messages
    }
}
