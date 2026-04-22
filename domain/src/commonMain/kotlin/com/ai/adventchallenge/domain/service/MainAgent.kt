package com.ai.adventchallenge.domain.service

import com.ai.adventchallenge.domain.model.Agent
import com.ai.adventchallenge.domain.model.AgentResponse
import com.ai.adventchallenge.domain.model.Message
import com.ai.adventchallenge.domain.model.Role

class MainAgent(
    private val aiService: AIService
) {
    suspend fun processRequest(
        agent: Agent,
        contextMessages: List<Message>
    ): AgentResponse {
        return try {
            val messagesToSend = mutableListOf<Message>()

            if (agent.settings.systemPrompt.isNotBlank()) {
                messagesToSend.add(
                    Message(
                        role = Role.SYSTEM,
                        content = agent.settings.systemPrompt
                    )
                )
            }

            messagesToSend.addAll(contextMessages)

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
}
